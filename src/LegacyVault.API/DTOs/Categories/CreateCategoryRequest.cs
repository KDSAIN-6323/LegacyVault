using System.ComponentModel.DataAnnotations;
using LegacyVault.API.Models.Enums;

namespace LegacyVault.API.DTOs.Categories;

public class CreateCategoryRequest
{
    [Required, MinLength(1), MaxLength(100)]
    public string Name { get; set; } = string.Empty;

    [MaxLength(10)]
    public string Icon { get; set; } = "📁";

    public CategoryType Type { get; set; } = CategoryType.General;

    public bool IsEncrypted { get; set; }

    public string? EncryptionSalt { get; set; }  // Required when IsEncrypted=true

    [MaxLength(200)]
    public string? PasswordHint { get; set; }
}
