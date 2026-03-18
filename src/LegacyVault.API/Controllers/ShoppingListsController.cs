using LegacyVault.API.Data;
using LegacyVault.API.Models.Enums;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace LegacyVault.API.Controllers;

[Authorize]
[Route("api/shopping-lists")]
public class ShoppingListsController(LegacyVaultDbContext db) : BaseApiController
{
    /// <summary>Returns all ShoppingList pages across every category owned by the current user.</summary>
    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var userId = CurrentUserId;
        var lists = await db.Pages
            .Include(p => p.Category)
            .Where(p => p.Type == PageType.ShoppingList && p.Category.UserId == userId)
            .OrderByDescending(p => p.UpdatedAt)
            .Select(p => new ShoppingListRefDto(p.Id, p.CategoryId, p.Title, p.IsEncrypted))
            .ToListAsync();

        return Ok(lists);
    }
}

public record ShoppingListRefDto(Guid Id, Guid CategoryId, string Title, bool IsEncrypted);
