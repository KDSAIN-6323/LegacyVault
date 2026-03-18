using LegacyVault.API.Data;
using LegacyVault.API.DTOs.Categories;
using LegacyVault.API.Models.Entities;
using LegacyVault.API.Models.Enums;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace LegacyVault.API.Controllers;

[Authorize]
public class CategoriesController(LegacyVaultDbContext db) : BaseApiController
{
    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var userId = CurrentUserId;
        var categories = await db.Categories
            .Where(c => c.UserId == userId)
            .OrderBy(c => c.Name)
            .Select(c => new CategoryDto
            {
                Id = c.Id,
                Type = c.Type,
                Name = c.Name,
                Icon = c.Icon,
                IsEncrypted = c.IsEncrypted,
                EncryptionSalt = c.EncryptionSalt,
                PasswordHint = c.PasswordHint,
                IsFavorite = c.IsFavorite,
                PageCount = c.Pages.Count,
                CreatedAt = c.CreatedAt,
                UpdatedAt = c.UpdatedAt
            })
            .ToListAsync();

        return Ok(categories);
    }

    [HttpGet("{id:guid}")]
    public async Task<IActionResult> GetById(Guid id)
    {
        var userId = CurrentUserId;
        var category = await db.Categories
            .Where(c => c.UserId == userId && c.Id == id)
            .Select(c => new CategoryDto
            {
                Id = c.Id,
                Type = c.Type,
                Name = c.Name,
                Icon = c.Icon,
                IsEncrypted = c.IsEncrypted,
                EncryptionSalt = c.EncryptionSalt,
                PasswordHint = c.PasswordHint,
                IsFavorite = c.IsFavorite,
                PageCount = c.Pages.Count,
                CreatedAt = c.CreatedAt,
                UpdatedAt = c.UpdatedAt
            })
            .FirstOrDefaultAsync();

        return category == null ? NotFound() : Ok(category);
    }

    [HttpPost]
    public async Task<IActionResult> Create([FromBody] CreateCategoryRequest request)
    {
        // Vaults are always encrypted
        if (request.Type == CategoryType.Vault)
            request.IsEncrypted = true;

        if (request.IsEncrypted && string.IsNullOrEmpty(request.EncryptionSalt))
            return BadRequest("EncryptionSalt is required when IsEncrypted is true.");

        var category = new Category
        {
            UserId = CurrentUserId,
            Type = request.Type,
            Name = request.Name,
            Icon = request.Type == CategoryType.Vault ? "🔑" : request.Icon,
            IsEncrypted = request.IsEncrypted,
            EncryptionSalt = request.IsEncrypted ? request.EncryptionSalt : null,
            PasswordHint = request.IsEncrypted ? request.PasswordHint?.Trim() : null
        };

        db.Categories.Add(category);
        await db.SaveChangesAsync();

        return CreatedAtAction(nameof(GetById), new { id = category.Id }, new CategoryDto
        {
            Id = category.Id,
            Type = category.Type,
            Name = category.Name,
            Icon = category.Icon,
            IsEncrypted = category.IsEncrypted,
            EncryptionSalt = category.EncryptionSalt,
            PasswordHint = category.PasswordHint,
            IsFavorite = category.IsFavorite,
            PageCount = 0,
            CreatedAt = category.CreatedAt,
            UpdatedAt = category.UpdatedAt
        });
    }

    [HttpPut("{id:guid}")]
    public async Task<IActionResult> Update(Guid id, [FromBody] UpdateCategoryRequest request)
    {
        var userId = CurrentUserId;
        var category = await db.Categories.FirstOrDefaultAsync(c => c.UserId == userId && c.Id == id);
        if (category == null) return NotFound();

        if (request.Name != null) category.Name = request.Name;
        if (request.Icon != null) category.Icon = request.Icon;
        category.UpdatedAt = DateTime.UtcNow;

        await db.SaveChangesAsync();
        return NoContent();
    }

    [HttpPatch("{id:guid}/favorite")]
    public async Task<IActionResult> ToggleFavorite(Guid id)
    {
        var userId = CurrentUserId;
        var category = await db.Categories.FirstOrDefaultAsync(c => c.UserId == userId && c.Id == id);
        if (category == null) return NotFound();

        category.IsFavorite = !category.IsFavorite;
        category.UpdatedAt = DateTime.UtcNow;
        await db.SaveChangesAsync();
        return Ok(new { isFavorite = category.IsFavorite });
    }

    [HttpDelete("{id:guid}")]
    public async Task<IActionResult> Delete(Guid id)
    {
        var userId = CurrentUserId;
        var category = await db.Categories.FirstOrDefaultAsync(c => c.UserId == userId && c.Id == id);
        if (category == null) return NotFound();

        db.Categories.Remove(category);
        await db.SaveChangesAsync();
        return NoContent();
    }
}
