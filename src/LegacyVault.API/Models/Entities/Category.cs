using LegacyVault.API.Models.Enums;

namespace LegacyVault.API.Models.Entities;

public class Category
{
    public Guid Id { get; set; } = Guid.NewGuid();
    public Guid UserId { get; set; }
    public CategoryType Type { get; set; } = CategoryType.General;
    public string Name { get; set; } = string.Empty;
    public string Icon { get; set; } = "📁";
    public bool IsEncrypted { get; set; }
    public string? EncryptionSalt { get; set; }  // Base64-encoded 256-bit salt
    public string? PasswordHint { get; set; }    // Optional hint shown on unlock prompt
    public bool IsFavorite { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

    public User User { get; set; } = null!;
    public ICollection<Page> Pages { get; set; } = new List<Page>();
}
