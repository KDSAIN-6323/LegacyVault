using System.ComponentModel.DataAnnotations;

namespace LegacyVault.API.DTOs.Pages;

public class UpdatePageRequest
{
    [MinLength(1), MaxLength(200)]
    public string? Title { get; set; }

    public string? Content { get; set; }
    public string? EncryptionIV { get; set; }
    public int? SortOrder { get; set; }
}
