using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace LegacyVault.API.Migrations
{
    /// <inheritdoc />
    public partial class AddIsFavorite : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<bool>(
                name: "IsFavorite",
                table: "Categories",
                type: "INTEGER",
                nullable: false,
                defaultValue: false);

            migrationBuilder.AddColumn<bool>(
                name: "IsFavorite",
                table: "Pages",
                type: "INTEGER",
                nullable: false,
                defaultValue: false);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "IsFavorite",
                table: "Categories");

            migrationBuilder.DropColumn(
                name: "IsFavorite",
                table: "Pages");
        }
    }
}
