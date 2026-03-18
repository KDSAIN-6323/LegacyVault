using LegacyVault.API.Models.Enums;

namespace LegacyVault.API.DTOs.Categories;

public class CategoryDto
{
    public Guid Id { get; set; }
    public CategoryType Type { get; set; }
    public string Name { get; set; } = string.Empty;
    public string Icon { get; set; } = string.Empty;
    public bool IsEncrypted { get; set; }
    public string? EncryptionSalt { get; set; }
    public string? PasswordHint { get; set; }
    public bool IsFavorite { get; set; }
    public int PageCount { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
}
