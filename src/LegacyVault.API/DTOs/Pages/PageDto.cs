using LegacyVault.API.Models.Enums;

namespace LegacyVault.API.DTOs.Pages;

public class PageDto
{
    public Guid Id { get; set; }
    public Guid CategoryId { get; set; }
    public PageType Type { get; set; }
    public string Title { get; set; } = string.Empty;
    public string Content { get; set; } = "{}";
    public bool IsEncrypted { get; set; }
    public string? EncryptionSalt { get; set; }
    public string? EncryptionIV { get; set; }
    public int SortOrder { get; set; }
    public bool IsFavorite { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
    public List<AttachmentDto> Attachments { get; set; } = [];
}

public class AttachmentDto
{
    public Guid Id { get; set; }
    public string FileName { get; set; } = string.Empty;
    public string MimeType { get; set; } = string.Empty;
    public long FileSize { get; set; }
    public string Url { get; set; } = string.Empty;  // /uploads/{FilePath}
}
