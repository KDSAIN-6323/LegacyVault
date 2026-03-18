using System.Security.Cryptography;
using System.Text;
using LegacyVault.API.Data;
using LegacyVault.API.DTOs.Auth;
using LegacyVault.API.Models.Entities;
using LegacyVault.API.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace LegacyVault.API.Controllers;

public class AuthController(LegacyVaultDbContext db, ITokenService tokenService, IConfiguration configuration) : BaseApiController
{
    [HttpPost("register")]
    public async Task<IActionResult> Register([FromBody] RegisterRequest request)
    {
        if (await db.Users.AnyAsync(u => u.Email == request.Email || u.Username == request.Username))
            return Conflict("Username or email already taken.");

        var user = new User
        {
            Username = request.Username,
            Email = request.Email.ToLowerInvariant(),
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.Password, workFactor: 12)
        };

        db.Users.Add(user);
        await db.SaveChangesAsync();

        return await IssueTokens(user);
    }

    [HttpPost("login")]
    public async Task<IActionResult> Login([FromBody] LoginRequest request)
    {
        var user = await db.Users.FirstOrDefaultAsync(u => u.Username == request.Username);
        if (user == null || !BCrypt.Net.BCrypt.Verify(request.Password, user.PasswordHash))
            return Unauthorized("Invalid username or password.");

        return await IssueTokens(user);
    }

    [HttpPost("refresh")]
    public async Task<IActionResult> Refresh()
    {
        var rawToken = Request.Cookies["refreshToken"];
        if (string.IsNullOrEmpty(rawToken))
            return Unauthorized("No refresh token.");

        var tokenHash = HashToken(rawToken);
        var storedToken = await db.RefreshTokens
            .Include(r => r.User)
            .FirstOrDefaultAsync(r => r.TokenHash == tokenHash);

        if (storedToken == null || !storedToken.IsActive)
            return Unauthorized("Invalid or expired refresh token.");

        // Rotate: revoke old, issue new
        storedToken.RevokedAt = DateTime.UtcNow;
        return await IssueTokens(storedToken.User);
    }

    [HttpPost("logout")]
    public async Task<IActionResult> Logout()
    {
        var rawToken = Request.Cookies["refreshToken"];
        if (!string.IsNullOrEmpty(rawToken))
        {
            var tokenHash = HashToken(rawToken);
            var token = await db.RefreshTokens.FirstOrDefaultAsync(r => r.TokenHash == tokenHash);
            if (token != null) token.RevokedAt = DateTime.UtcNow;
            await db.SaveChangesAsync();
        }

        Response.Cookies.Delete("refreshToken");
        return NoContent();
    }

    [HttpPost("reset-password")]
    public async Task<IActionResult> ResetPassword([FromBody] ResetPasswordRequest request)
    {
        var user = await db.Users.FirstOrDefaultAsync(u =>
            u.Username == request.Username &&
            u.Email == request.Email.ToLowerInvariant());

        if (user == null)
            return BadRequest("No account found with that username and email combination.");

        user.PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.NewPassword, workFactor: 12);
        await db.SaveChangesAsync();
        return Ok(new { message = "Password reset successfully." });
    }

    [HttpGet("health")]
    public IActionResult Health() => Ok(new { status = "healthy", version = "1.0.0" });

    private async Task<IActionResult> IssueTokens(User user)
    {
        var accessToken = tokenService.GenerateAccessToken(user);
        var rawRefreshToken = tokenService.GenerateRefreshToken();

        var expiryDays = int.Parse(configuration["JwtSettings:RefreshTokenExpiryDays"] ?? "30");
        var refreshToken = new RefreshToken
        {
            UserId = user.Id,
            TokenHash = HashToken(rawRefreshToken),
            ExpiresAt = DateTime.UtcNow.AddDays(expiryDays)
        };

        db.RefreshTokens.Add(refreshToken);
        await db.SaveChangesAsync();

        Response.Cookies.Append("refreshToken", rawRefreshToken, new CookieOptions
        {
            HttpOnly = true,
            Secure = false,  // Set true in production with HTTPS
            SameSite = SameSiteMode.Strict,
            Expires = refreshToken.ExpiresAt
        });

        return Ok(new AuthResponse
        {
            AccessToken = accessToken,
            User = new UserDto { Id = user.Id, Username = user.Username, Email = user.Email }
        });
    }

    private static string HashToken(string token)
    {
        var bytes = SHA256.HashData(Encoding.UTF8.GetBytes(token));
        return Convert.ToBase64String(bytes);
    }
}
