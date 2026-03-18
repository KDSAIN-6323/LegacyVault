using System.ComponentModel.DataAnnotations;

namespace LegacyVault.API.DTOs.Categories;

public class UpdateCategoryRequest
{
    [MinLength(1), MaxLength(100)]
    public string? Name { get; set; }

    [MaxLength(10)]
    public string? Icon { get; set; }
}
