using System.ComponentModel.DataAnnotations;

namespace LegacyVault.API.DTOs.Pages;

public class MovePageRequest
{
    [Required]
    public Guid TargetCategoryId { get; set; }
}
