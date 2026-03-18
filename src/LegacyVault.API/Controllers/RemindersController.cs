using LegacyVault.API.Data;
using LegacyVault.API.Models.Enums;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace LegacyVault.API.Controllers;

[Authorize]
public class RemindersController(LegacyVaultDbContext db) : BaseApiController
{
    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var userId = CurrentUserId;
        var reminders = await db.Pages
            .Include(p => p.Category)
            .Where(p => p.Category.UserId == userId
                     && p.Type == PageType.Reminder
                     && !p.IsEncrypted)
            .Select(p => new ReminderPageDto(
                p.Id,
                p.CategoryId,
                p.Category.Name,
                p.Category.Icon,
                p.Title,
                p.Content))
            .ToListAsync();

        return Ok(reminders);
    }
}

public record ReminderPageDto(
    Guid PageId,
    Guid CategoryId,
    string CategoryName,
    string CategoryIcon,
    string Title,
    string Content);
