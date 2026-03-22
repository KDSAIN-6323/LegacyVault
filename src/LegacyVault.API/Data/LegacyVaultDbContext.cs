using LegacyVault.API.Models.Entities;
using Microsoft.EntityFrameworkCore;

namespace LegacyVault.API.Data;

public class LegacyVaultDbContext(DbContextOptions<LegacyVaultDbContext> options) : DbContext(options)
{
    public DbSet<User> Users => Set<User>();
    public DbSet<Category> Categories => Set<Category>();
    public DbSet<Page> Pages => Set<Page>();
    public DbSet<Attachment> Attachments => Set<Attachment>();
    public DbSet<RefreshToken> RefreshTokens => Set<RefreshToken>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<User>(entity =>
        {
            entity.HasIndex(u => u.Email).IsUnique();
            entity.HasIndex(u => u.Username).IsUnique();
        });

        modelBuilder.Entity<Category>(entity =>
        {
            entity.HasOne(c => c.User)
                .WithMany(u => u.Categories)
                .HasForeignKey(c => c.UserId)
                .OnDelete(DeleteBehavior.Cascade);
            entity.HasIndex(c => new { c.UserId, c.Name });
            entity.Property(c => c.Type).HasConversion<string>();
        });

        modelBuilder.Entity<Page>(entity =>
        {
            entity.HasOne(p => p.Category)
                .WithMany(c => c.Pages)
                .HasForeignKey(p => p.CategoryId)
                .OnDelete(DeleteBehavior.Cascade);
            entity.HasIndex(p => p.CategoryId);
            entity.Property(p => p.Type).HasConversion<string>();
        });

        modelBuilder.Entity<Attachment>(entity =>
        {
            entity.HasOne(a => a.Page)
                .WithMany(p => p.Attachments)
                .HasForeignKey(a => a.PageId)
                .OnDelete(DeleteBehavior.Cascade);
            entity.HasIndex(a => a.PageId);
        });

        modelBuilder.Entity<RefreshToken>(entity =>
        {
            entity.HasOne(r => r.User)
                .WithMany(u => u.RefreshTokens)
                .HasForeignKey(r => r.UserId)
                .OnDelete(DeleteBehavior.Cascade);
            entity.HasIndex(r => r.UserId);
            entity.HasIndex(r => r.TokenHash).IsUnique();
        });
    }

    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
    {
        // Enable WAL mode for better concurrent read performance
        optionsBuilder.UseSqlite(o => o.CommandTimeout(30));
    }
}
