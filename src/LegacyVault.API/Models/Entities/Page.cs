using LegacyVault.API.Models.Enums;

namespace LegacyVault.API.Models.Entities;

public class Page
{
    public Guid Id { get; set; } = Guid.NewGuid();
    public Guid CategoryId { get; set; }
    public PageType Type { get; set; }
    public string Title { get; set; } = string.Empty;
    public string Content { get; set; } = "{}";  // JSON plaintext or Base64 ciphertext
    public bool IsEncrypted { get; set; }
    public string? EncryptionSalt { get; set; }  // Only set for page-level (not category-level) encryption
    public string? EncryptionIV { get; set; }    // Base64-encoded 96-bit AES-GCM IV
    public int SortOrder { get; set; }
    public bool IsFavorite { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

    public Category Category { get; set; } = null!;
    public ICollection<Attachment> Attachments { get; set; } = new List<Attachment>();
}
