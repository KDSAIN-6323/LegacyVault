using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace LegacyVault.API.Migrations
{
    /// <inheritdoc />
    public partial class AddCategoryPasswordHint : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "PasswordHint",
                table: "Categories",
                type: "TEXT",
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "PasswordHint",
                table: "Categories");
        }
    }
}
