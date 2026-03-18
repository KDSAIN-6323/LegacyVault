using LegacyVault.API.Data;
using LegacyVault.API.DTOs.Pages;
using LegacyVault.API.Models.Entities;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace LegacyVault.API.Controllers;

[Authorize]
public class AttachmentsController(LegacyVaultDbContext db, IWebHostEnvironment env) : BaseApiController
{
    private static readonly string[] AllowedMimeTypes =
    [
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "image/heic", "image/heif", "application/pdf"
    ];

    [HttpPost("~/api/categories/{categoryId:guid}/pages/{pageId:guid}/attachments")]
    public async Task<IActionResult> Upload(Guid categoryId, Guid pageId, IFormFile file)
    {
        var userId = CurrentUserId;
        var page = await db.Pages
            .Include(p => p.Category)
            .FirstOrDefaultAsync(p => p.Id == pageId && p.CategoryId == categoryId && p.Category.UserId == userId);

        if (page == null) return NotFound();

        if (file.Length == 0) return BadRequest("File is empty.");
        if (file.Length > 20 * 1024 * 1024) return BadRequest("File exceeds 20MB limit.");
        if (!AllowedMimeTypes.Contains(file.ContentType)) return BadRequest("File type not allowed.");

        var uploadsRoot = Environment.GetEnvironmentVariable("VAULT_UPLOADS_PATH")
            ?? Path.Combine(env.ContentRootPath, "uploads");
        Directory.CreateDirectory(uploadsRoot);

        var uniqueName = $"{Guid.NewGuid()}{Path.GetExtension(file.FileName)}";
        var fullPath = Path.Combine(uploadsRoot, uniqueName);

        await using (var stream = System.IO.File.Create(fullPath))
            await file.CopyToAsync(stream);

        var attachment = new Attachment
        {
            PageId = pageId,
            FileName = file.FileName,
            FilePath = uniqueName,
            MimeType = file.ContentType,
            FileSize = file.Length
        };

        db.Attachments.Add(attachment);
        await db.SaveChangesAsync();

        return Ok(new AttachmentDto
        {
            Id = attachment.Id,
            FileName = attachment.FileName,
            MimeType = attachment.MimeType,
            FileSize = attachment.FileSize,
            Url = $"/uploads/{uniqueName}"
        });
    }

    [HttpDelete("~/api/attachments/{id:guid}")]
    public async Task<IActionResult> Delete(Guid id)
    {
        var userId = CurrentUserId;
        var attachment = await db.Attachments
            .Include(a => a.Page).ThenInclude(p => p.Category)
            .FirstOrDefaultAsync(a => a.Id == id && a.Page.Category.UserId == userId);

        if (attachment == null) return NotFound();

        var fullPath = Path.Combine(
            Environment.GetEnvironmentVariable("VAULT_UPLOADS_PATH") ?? Path.Combine(env.ContentRootPath, "uploads"),
            attachment.FilePath);
        if (System.IO.File.Exists(fullPath))
            System.IO.File.Delete(fullPath);

        db.Attachments.Remove(attachment);
        await db.SaveChangesAsync();
        return NoContent();
    }
}
