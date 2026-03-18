using LegacyVault.API.Data;
using LegacyVault.API.DTOs.Search;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace LegacyVault.API.Controllers;

[Authorize]
[Route("api/search")]
public class SearchController(LegacyVaultDbContext db) : BaseApiController
{
    [HttpGet]
    public async Task<IActionResult> Search([FromQuery] string? q)
    {
        if (string.IsNullOrWhiteSpace(q) || q.Trim().Length < 2)
            return Ok(Array.Empty<SearchResultDto>());

        var userId = CurrentUserId;
        var term = q.Trim();

        var results = await db.Pages
            .Where(p => p.Category.UserId == userId &&
                        (EF.Functions.Like(p.Title, $"%{term}%") ||
                         (!p.IsEncrypted && EF.Functions.Like(p.Content, $"%{term}%"))))
            .OrderByDescending(p => p.UpdatedAt)
            .Take(50)
            .Select(p => new SearchResultDto
            {
                PageId = p.Id,
                CategoryId = p.CategoryId,
                CategoryName = p.Category.Name,
                CategoryIcon = p.Category.Icon,
                Type = p.Type.ToString(),
                Title = p.Title,
                IsEncrypted = p.IsEncrypted,
                UpdatedAt = p.UpdatedAt
            })
            .ToListAsync();

        return Ok(results);
    }
}
