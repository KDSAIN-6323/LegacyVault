using System.Security.Cryptography;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.Sqlite;

namespace LegacyVault.API.Controllers;

[Authorize]
public class BackupController(IConfiguration config, IWebHostEnvironment env) : BaseApiController
{
    private const int SaltSize = 16;
    private const int NonceSize = 12;
    private const int TagSize = 16;
    private const int KeySize = 32;
    private const int Pbkdf2Iterations = 310_000; // OWASP 2023 recommendation (matches frontend)

    private string BackupsPath =>
        Environment.GetEnvironmentVariable("VAULT_BACKUPS_PATH")
        ?? Path.Combine(env.ContentRootPath, "backups");

    private string DbPath
    {
        get
        {
            var cs = config.GetConnectionString("DefaultConnection")
                ?? "Data Source=/app/data/legacyvault.db";
            const string prefix = "Data Source=";
            if (!cs.StartsWith(prefix, StringComparison.OrdinalIgnoreCase))
                return "/app/data/legacyvault.db";
            var ds = cs[prefix.Length..].Trim();
            if (!Path.IsPathRooted(ds))
                ds = Path.GetFullPath(Path.Combine(env.ContentRootPath, ds));
            return ds;
        }
    }

    [HttpGet]
    public IActionResult List()
    {
        Directory.CreateDirectory(BackupsPath);
        var entries = new DirectoryInfo(BackupsPath)
            .GetFiles("*.vaultbak")
            .OrderByDescending(f => f.CreationTimeUtc)
            .Select(f => new BackupEntryDto(f.Name, f.Length, f.CreationTimeUtc));
        return Ok(entries);
    }

    [HttpPost]
    public IActionResult Create([FromBody] BackupPasswordDto dto)
    {
        if (string.IsNullOrWhiteSpace(dto.Password))
            return BadRequest("Password is required.");

        Directory.CreateDirectory(BackupsPath);
        var timestamp = DateTime.UtcNow.ToString("yyyyMMdd_HHmmss");
        var tempFile = Path.Combine(BackupsPath, $"tmp_{timestamp}.db");
        var backupFile = Path.Combine(BackupsPath, $"legacyvault_backup_{timestamp}.vaultbak");

        try
        {
            // Hot backup to temp SQLite file
            using (var src = new SqliteConnection($"Data Source={DbPath}"))
            using (var dst = new SqliteConnection($"Data Source={tempFile}"))
            {
                src.Open();
                dst.Open();
                src.BackupDatabase(dst);
            }

            // Encrypt and write to .vaultbak
            var plainBytes = System.IO.File.ReadAllBytes(tempFile);
            var encrypted = Encrypt(plainBytes, dto.Password);
            System.IO.File.WriteAllBytes(backupFile, encrypted);
        }
        finally
        {
            if (System.IO.File.Exists(tempFile))
                System.IO.File.Delete(tempFile);
        }

        var info = new FileInfo(backupFile);
        return Ok(new BackupEntryDto(info.Name, info.Length, info.CreationTimeUtc));
    }

    [HttpPost("restore/{fileName}")]
    public IActionResult Restore(string fileName, [FromBody] BackupPasswordDto dto)
    {
        // Sanitize: strip any directory components, then validate extension
        var safeName = Path.GetFileName(fileName);
        if (string.IsNullOrEmpty(safeName) || !safeName.EndsWith(".vaultbak", StringComparison.OrdinalIgnoreCase))
            return BadRequest("Invalid file name.");

        if (string.IsNullOrWhiteSpace(dto.Password))
            return BadRequest("Password is required.");

        var backupFile = Path.Combine(BackupsPath, safeName);
        if (!System.IO.File.Exists(backupFile))
            return NotFound("Backup file not found.");

        byte[] decrypted;
        try
        {
            var encryptedBytes = System.IO.File.ReadAllBytes(backupFile);
            decrypted = Decrypt(encryptedBytes, dto.Password);
        }
        catch (CryptographicException)
        {
            return BadRequest("Incorrect password or corrupted backup file.");
        }

        var tempFile = Path.Combine(Path.GetTempPath(), $"lv_restore_{Guid.NewGuid()}.db");
        try
        {
            System.IO.File.WriteAllBytes(tempFile, decrypted);
            SqliteConnection.ClearAllPools();
            System.IO.File.Copy(tempFile, DbPath, overwrite: true);
        }
        finally
        {
            if (System.IO.File.Exists(tempFile))
                System.IO.File.Delete(tempFile);
        }

        return Ok(new { message = "Database restored successfully." });
    }

    // Layout: [16 salt][12 nonce][16 tag][ciphertext]
    private static byte[] Encrypt(byte[] plaintext, string password)
    {
        var salt = RandomNumberGenerator.GetBytes(SaltSize);
        var nonce = RandomNumberGenerator.GetBytes(NonceSize);
        var key = DeriveKey(password, salt);
        var ciphertext = new byte[plaintext.Length];
        var tag = new byte[TagSize];

        using var aes = new AesGcm(key, TagSize);
        aes.Encrypt(nonce, plaintext, ciphertext, tag);

        var result = new byte[SaltSize + NonceSize + TagSize + ciphertext.Length];
        Buffer.BlockCopy(salt,       0, result, 0,                              SaltSize);
        Buffer.BlockCopy(nonce,      0, result, SaltSize,                       NonceSize);
        Buffer.BlockCopy(tag,        0, result, SaltSize + NonceSize,           TagSize);
        Buffer.BlockCopy(ciphertext, 0, result, SaltSize + NonceSize + TagSize, ciphertext.Length);
        return result;
    }

    private static byte[] Decrypt(byte[] data, string password)
    {
        if (data.Length < SaltSize + NonceSize + TagSize)
            throw new CryptographicException("Data too short to be a valid backup.");

        var salt       = data[..SaltSize];
        var nonce      = data[SaltSize..(SaltSize + NonceSize)];
        var tag        = data[(SaltSize + NonceSize)..(SaltSize + NonceSize + TagSize)];
        var ciphertext = data[(SaltSize + NonceSize + TagSize)..];
        var key        = DeriveKey(password, salt);
        var plaintext  = new byte[ciphertext.Length];

        using var aes = new AesGcm(key, TagSize);
        aes.Decrypt(nonce, ciphertext, tag, plaintext); // throws CryptographicException on wrong password
        return plaintext;
    }

    private static byte[] DeriveKey(string password, byte[] salt)
    {
        return Rfc2898DeriveBytes.Pbkdf2(password, salt, Pbkdf2Iterations, HashAlgorithmName.SHA256, KeySize);
    }
}

public record BackupEntryDto(string FileName, long FileSizeBytes, DateTime CreatedAt);
public record BackupPasswordDto(string Password);
