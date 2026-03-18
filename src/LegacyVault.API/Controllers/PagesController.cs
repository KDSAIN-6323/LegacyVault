using LegacyVault.API.Data;
using LegacyVault.API.DTOs.Pages;
using LegacyVault.API.Models.Entities;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace LegacyVault.API.Controllers;

[Authorize]
[Route("api/categories/{categoryId:guid}/pages")]
public class PagesController(LegacyVaultDbContext db) : BaseApiController
{
    [HttpGet]
    public async Task<IActionResult> GetAll(Guid categoryId)
    {
        if (!await OwnsCategoryAsync(categoryId)) return NotFound();

        var pages = await db.Pages
            .Where(p => p.CategoryId == categoryId)
            .OrderBy(p => p.SortOrder).ThenByDescending(p => p.UpdatedAt)
            .Select(p => new PageDto
            {
                Id = p.Id,
                CategoryId = p.CategoryId,
                Type = p.Type,
                Title = p.Title,
                Content = p.Content,
                IsEncrypted = p.IsEncrypted,
                EncryptionSalt = p.EncryptionSalt,
                EncryptionIV = p.EncryptionIV,
                SortOrder = p.SortOrder,
                IsFavorite = p.IsFavorite,
                CreatedAt = p.CreatedAt,
                UpdatedAt = p.UpdatedAt,
                Attachments = p.Attachments.Select(a => new AttachmentDto
                {
                    Id = a.Id,
                    FileName = a.FileName,
                    MimeType = a.MimeType,
                    FileSize = a.FileSize,
                    Url = $"/uploads/{a.FilePath}"
                }).ToList()
            })
            .ToListAsync();

        return Ok(pages);
    }

    [HttpGet("{id:guid}")]
    public async Task<IActionResult> GetById(Guid categoryId, Guid id)
    {
        if (!await OwnsCategoryAsync(categoryId)) return NotFound();

        var page = await db.Pages
            .Include(p => p.Attachments)
            .FirstOrDefaultAsync(p => p.CategoryId == categoryId && p.Id == id);

        if (page == null) return NotFound();

        return Ok(MapToDto(page));
    }

    [HttpPost]
    public async Task<IActionResult> Create(Guid categoryId, [FromBody] CreatePageRequest request)
    {
        if (!await OwnsCategoryAsync(categoryId)) return NotFound();

        var page = new Page
        {
            CategoryId = categoryId,
            Type = request.Type,
            Title = request.Title,
            Content = request.Content,
            IsEncrypted = request.IsEncrypted,
            EncryptionSalt = request.EncryptionSalt,
            EncryptionIV = request.EncryptionIV
        };

        db.Pages.Add(page);
        await db.SaveChangesAsync();

        return CreatedAtAction(nameof(GetById), new { categoryId, id = page.Id }, MapToDto(page));
    }

    [HttpPut("{id:guid}")]
    public async Task<IActionResult> Update(Guid categoryId, Guid id, [FromBody] UpdatePageRequest request)
    {
        if (!await OwnsCategoryAsync(categoryId)) return NotFound();

        var page = await db.Pages.FirstOrDefaultAsync(p => p.CategoryId == categoryId && p.Id == id);
        if (page == null) return NotFound();

        if (request.Title != null) page.Title = request.Title;
        if (request.Content != null) page.Content = request.Content;
        if (request.EncryptionIV != null) page.EncryptionIV = request.EncryptionIV;
        if (request.SortOrder.HasValue) page.SortOrder = request.SortOrder.Value;
        page.UpdatedAt = DateTime.UtcNow;

        await db.SaveChangesAsync();
        return NoContent();
    }

    [HttpPatch("{id:guid}/favorite")]
    public async Task<IActionResult> ToggleFavorite(Guid categoryId, Guid id)
    {
        if (!await OwnsCategoryAsync(categoryId)) return NotFound();

        var page = await db.Pages.FirstOrDefaultAsync(p => p.CategoryId == categoryId && p.Id == id);
        if (page == null) return NotFound();

        page.IsFavorite = !page.IsFavorite;
        page.UpdatedAt = DateTime.UtcNow;
        await db.SaveChangesAsync();
        return Ok(new { isFavorite = page.IsFavorite });
    }

    [HttpPatch("{id:guid}/move")]
    public async Task<IActionResult> Move(Guid categoryId, Guid id, [FromBody] MovePageRequest request)
    {
        if (!await OwnsCategoryAsync(categoryId)) return NotFound();
        if (!await OwnsCategoryAsync(request.TargetCategoryId))
            return BadRequest("Target category not found.");

        var page = await db.Pages.FirstOrDefaultAsync(p => p.CategoryId == categoryId && p.Id == id);
        if (page == null) return NotFound();

        page.CategoryId = request.TargetCategoryId;
        page.UpdatedAt = DateTime.UtcNow;

        await db.SaveChangesAsync();
        return NoContent();
    }

    [HttpDelete("{id:guid}")]
    public async Task<IActionResult> Delete(Guid categoryId, Guid id)
    {
        if (!await OwnsCategoryAsync(categoryId)) return NotFound();

        var page = await db.Pages.FirstOrDefaultAsync(p => p.CategoryId == categoryId && p.Id == id);
        if (page == null) return NotFound();

        db.Pages.Remove(page);
        await db.SaveChangesAsync();
        return NoContent();
    }

    private async Task<bool> OwnsCategoryAsync(Guid categoryId)
    {
        var userId = CurrentUserId;
        return await db.Categories.AnyAsync(c => c.Id == categoryId && c.UserId == userId);
    }

    private static PageDto MapToDto(Page page) => new()
    {
        Id = page.Id,
        CategoryId = page.CategoryId,
        Type = page.Type,
        Title = page.Title,
        Content = page.Content,
        IsEncrypted = page.IsEncrypted,
        EncryptionSalt = page.EncryptionSalt,
        EncryptionIV = page.EncryptionIV,
        SortOrder = page.SortOrder,
        IsFavorite = page.IsFavorite,
        CreatedAt = page.CreatedAt,
        UpdatedAt = page.UpdatedAt,
        Attachments = page.Attachments.Select(a => new AttachmentDto
        {
            Id = a.Id,
            FileName = a.FileName,
            MimeType = a.MimeType,
            FileSize = a.FileSize,
            Url = $"/uploads/{a.FilePath}"
        }).ToList()
    };
}
