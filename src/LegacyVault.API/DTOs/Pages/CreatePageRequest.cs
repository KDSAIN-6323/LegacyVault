using System.ComponentModel.DataAnnotations;
using LegacyVault.API.Models.Enums;

namespace LegacyVault.API.DTOs.Pages;

public class CreatePageRequest
{
    [Required, MinLength(1), MaxLength(200)]
    public string Title { get; set; } = string.Empty;

    [Required]
    public PageType Type { get; set; }

    public string Content { get; set; } = "{}";
    public bool IsEncrypted { get; set; }
    public string? EncryptionSalt { get; set; }
    public string? EncryptionIV { get; set; }
}
