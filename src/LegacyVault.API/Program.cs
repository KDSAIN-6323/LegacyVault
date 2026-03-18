using System.Text;
using LegacyVault.API.Data;
using LegacyVault.API.Services;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;

var builder = WebApplication.CreateBuilder(args);

// Database — resolve relative Data Source paths against ContentRootPath so the
// database location is stable regardless of the process working directory.
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection")
    ?? "Data Source=/app/data/legacyvault.db";

const string dataSourcePrefix = "Data Source=";
if (connectionString.StartsWith(dataSourcePrefix, StringComparison.OrdinalIgnoreCase))
{
    var dataSource = connectionString[dataSourcePrefix.Length..].Trim();
    if (!Path.IsPathRooted(dataSource))
    {
        dataSource = Path.GetFullPath(Path.Combine(builder.Environment.ContentRootPath, dataSource));
        connectionString = dataSourcePrefix + dataSource;
    }
    Directory.CreateDirectory(Path.GetDirectoryName(dataSource)!);
}

builder.Services.AddDbContext<LegacyVaultDbContext>(options =>
    options.UseSqlite(connectionString));

// JWT Auth
var jwtSecret = builder.Configuration["JwtSettings:Secret"]
    ?? throw new InvalidOperationException("JwtSettings:Secret must be configured");
if (jwtSecret.Length < 32)
    throw new InvalidOperationException("JwtSettings:Secret must be at least 32 characters.");
builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtSecret)),
            ValidateIssuer = true,
            ValidIssuer = builder.Configuration["JwtSettings:Issuer"],
            ValidateAudience = true,
            ValidAudience = builder.Configuration["JwtSettings:Audience"],
            ValidateLifetime = true,
            ClockSkew = TimeSpan.FromSeconds(30)
        };
    });

builder.Services.AddAuthorization();
builder.Services.AddScoped<ITokenService, TokenService>();
builder.Services.AddControllers()
    .AddJsonOptions(opts =>
        opts.JsonSerializerOptions.Converters.Add(new System.Text.Json.Serialization.JsonStringEnumConverter()));
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// Auto-migrate on startup and configure SQLite pragmas
using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<LegacyVaultDbContext>();
    db.Database.Migrate();
    db.Database.ExecuteSqlRaw("PRAGMA journal_mode=WAL;");
    db.Database.ExecuteSqlRaw("PRAGMA synchronous=NORMAL;");
}

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

// Serve uploaded files at /uploads/*
// VAULT_UPLOADS_PATH env var overrides the default path (used when bind-mounting a host folder)
var uploadsPath = Environment.GetEnvironmentVariable("VAULT_UPLOADS_PATH")
    ?? Path.Combine(builder.Environment.ContentRootPath, "uploads");
Directory.CreateDirectory(uploadsPath);

// Ensure backups directory exists
var backupsPath = Environment.GetEnvironmentVariable("VAULT_BACKUPS_PATH")
    ?? Path.Combine(builder.Environment.ContentRootPath, "backups");
Directory.CreateDirectory(backupsPath);
app.UseStaticFiles(new StaticFileOptions
{
    FileProvider = new Microsoft.Extensions.FileProviders.PhysicalFileProvider(uploadsPath),
    RequestPath = "/uploads"
});

app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();
