FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src
COPY ["src/LegacyVault.API/LegacyVault.API.csproj", "src/LegacyVault.API/"]
RUN dotnet restore "src/LegacyVault.API/LegacyVault.API.csproj"
COPY . .
WORKDIR "/src/src/LegacyVault.API"
RUN dotnet publish "LegacyVault.API.csproj" -c Release -o /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS final
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends wget && rm -rf /var/lib/apt/lists/*
RUN mkdir -p /app/data /app/uploads
COPY --from=build /app/publish .
EXPOSE 5000
ENV ASPNETCORE_URLS=http://+:5000
ENTRYPOINT ["dotnet", "LegacyVault.API.dll"]
