using System.ComponentModel.DataAnnotations;

namespace LegacyVault.API.DTOs.Auth;

public class ResetPasswordRequest
{
    [Required]
    public string Username { get; set; } = string.Empty;

    [Required, EmailAddress]
    public string Email { get; set; } = string.Empty;

    [Required, MinLength(8)]
    public string NewPassword { get; set; } = string.Empty;
}
