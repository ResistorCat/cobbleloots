#!/usr/bin/env python3
"""
Publisher utilities for Cobbleloots.
"""

import subprocess
import typer
from rich import print
from config import (
    load_env,
    load_mod_properties,
    load_changelog,
    ROOT_PATH,
    load_modinfo,
)
from modrinth import (
    upload_to_modrinth,
    upload_modinfo_to_modrinth,
    fetch_modrinth_version,
)
from curseforge import upload_to_curseforge


app = typer.Typer()


@app.command()
def build(fabric: bool = True, neoforge: bool = True) -> None:
    """
    Build the mod for Fabric and/or NeoForge.
    """
    # Load mod properties
    mod_properties = load_mod_properties()

    # Print build info and ask for confirmation
    print(
        f"[blue]Building {mod_properties.mod_name} v{mod_properties.mod_version} ({mod_properties.mod_version_type})...[/blue]"
    )
    confirm = typer.confirm("Do you want to continue?", default=True)
    if not confirm:
        print("[yellow]Build cancelled.[/yellow]")
        raise typer.Exit()

    # Build the mod
    if fabric and neoforge:
        print("[blue]Building for Fabric and NeoForge...[/blue]")
        command = ["./gradlew", "remapJar"]
    elif fabric:
        print("[blue]Building for Fabric...[/blue]")
        command = ["./gradlew", "fabric:remapJar"]
    elif neoforge:
        print("[blue]Building for NeoForge...[/blue]")
        command = ["./gradlew", "neoforge:remapJar"]
    else:
        print("[red]Error: Must specify at least one platform to build for.[/red]")
        raise typer.Exit(code=1)

    result = subprocess.run(command, capture_output=True, text=True)
    if result.returncode != 0:
        print(result.stdout)
        print(result.stderr)
        print("[red]Build failed![/red]")
        raise typer.Exit(code=1)
    print("[green]Build succeeded![/green]")


@app.command()
def publish(modrinth: bool = True, curseforge: bool = True) -> None:
    """
    Publish the mod to Modrinth and/or CurseForge.
    """
    # Load mod properties
    mod_properties = load_mod_properties()

    # Build the mod before publishing
    build()

    # Load changelog
    changelog = load_changelog(mod_properties.mod_version_type)
    if changelog:
        print(f"[blue]Changelog loaded:[/blue]\n{changelog}")
        confirm = typer.confirm("Do you want to continue?", default=True)
        if not confirm:
            print("[yellow]Publish cancelled.[/yellow]")
            raise typer.Exit()
    else:
        print("[red]Error: Changelog is empty or could not be loaded.[/red]")
        raise typer.Exit(code=1)

    # Load modinfo
    modinfo = load_modinfo()
    if modinfo:
        print(f"[blue]Mod info loaded:[/blue]\n{modinfo}")
        confirm = typer.confirm("Do you want to continue?", default=True)
        if not confirm:
            print("[yellow]Publish cancelled.[/yellow]")
            raise typer.Exit()
    else:
        print("[red]Error: Mod info is empty or could not be loaded.[/red]")
        raise typer.Exit(code=1)

    # Publish
    if modrinth:
        # Check if version already exists on Modrinth
        version_info = fetch_modrinth_version(mod_properties.mod_version)
        if version_info.status_code == 200:
            print(
                f"[red]Error: Version {mod_properties.mod_version} already exists on Modrinth.[/red]"
            )
        else:
            # Publish Fabric version
            print("[blue]Publishing Fabric version to Modrinth...[/blue]")
            fabric_path = (
                ROOT_PATH
                / "fabric"
                / "build"
                / "libs"
                / f"{mod_properties.mod_id}-fabric-{mod_properties.mod_version}.jar"
            )
            if not fabric_path.exists():
                print(f"[red]Error: Fabric build not found at {fabric_path}[/red]")
                raise typer.Exit(code=1)

            try:
                response = upload_to_modrinth(
                    fabric_path, mod_properties, "fabric", changelog
                )
                if response and response.status_code == 200:
                    print("[green]Fabric version published successfully![/green]")
                else:
                    print("[red]Failed to publish Fabric version.[/red]")
            except Exception as e:
                print(
                    f"[red]Exception occurred while publishing Fabric version: {e}[/red]"
                )
            finally:
                confirm = typer.confirm("Do you want to continue?", default=True)
                if not confirm:
                    print("[yellow]Publish cancelled.[/yellow]")
                    raise typer.Exit()

            # Publish NeoForge version
            print("[blue]Publishing NeoForge version to Modrinth...[/blue]")
            neoforge_path = (
                ROOT_PATH
                / "neoforge"
                / "build"
                / "libs"
                / f"{mod_properties.mod_id}-neoforge-{mod_properties.mod_version}.jar"
            )
            if not neoforge_path.exists():
                print(f"[red]Error: NeoForge build not found at {neoforge_path}[/red]")
                raise typer.Exit(code=1)
            try:
                response = upload_to_modrinth(
                    neoforge_path, mod_properties, "neoforge", changelog
                )
                if response and response.status_code == 200:
                    print("[green]NeoForge version published successfully![/green]")
                else:
                    print("[red]Failed to publish NeoForge version.[/red]")
            except Exception as e:
                print(
                    f"[red]Exception occurred while publishing NeoForge version: {e}[/red]"
                )
            finally:
                confirm = typer.confirm("Do you want to continue?", default=True)
                if not confirm:
                    print("[yellow]Publish cancelled.[/yellow]")
                    raise typer.Exit()

        # Update modinfo on Modrinth
        print("[blue]Updating modinfo on Modrinth...[/blue]")
        if not confirm:
            print("[yellow]Update cancelled.[/yellow]")
            raise typer.Exit()
        if modinfo:
            try:
                response = upload_modinfo_to_modrinth(modinfo)
                print("[green]Mod info updated successfully![/green]")
            except Exception as e:
                print(f"[red]Exception occurred while updating mod info: {e}[/red]")

    # Publish
    if curseforge:
        # Ask to continue
        confirm = typer.confirm(
            f"Do you want to publish version {mod_properties.mod_version} to CurseForge?",
            default=True,
        )
        if not confirm:
            print("[yellow]Publish cancelled.[/yellow]")
        else:
            # Publish Fabric version
            print("[blue]Publishing Fabric version to Curseforge...[/blue]")
            fabric_path = (
                ROOT_PATH
                / "fabric"
                / "build"
                / "libs"
                / f"{mod_properties.mod_id}-fabric-{mod_properties.mod_version}.jar"
            )
            if not fabric_path.exists():
                print(f"[red]Error: Fabric build not found at {fabric_path}[/red]")
                raise typer.Exit(code=1)

            try:
                response = upload_to_curseforge(
                    fabric_path, mod_properties, "fabric", changelog
                )
                if response and response.status_code == 200:
                    print("[green]Fabric version published successfully![/green]")
                else:
                    print("[red]Failed to publish Fabric version.[/red]")
            except Exception as e:
                print(
                    f"[red]Exception occurred while publishing Fabric version: {e}[/red]"
                )
            finally:
                confirm = typer.confirm("Do you want to continue?", default=True)
                if not confirm:
                    print("[yellow]Publish cancelled.[/yellow]")
                    raise typer.Exit()

            # Publish NeoForge version
            print("[blue]Publishing NeoForge version to Modrinth...[/blue]")
            neoforge_path = (
                ROOT_PATH
                / "neoforge"
                / "build"
                / "libs"
                / f"{mod_properties.mod_id}-neoforge-{mod_properties.mod_version}.jar"
            )
            if not neoforge_path.exists():
                print(f"[red]Error: NeoForge build not found at {neoforge_path}[/red]")
                raise typer.Exit(code=1)
            try:
                response = upload_to_curseforge(
                    neoforge_path, mod_properties, "neoforge", changelog
                )
                if response and response.status_code == 200:
                    print("[green]NeoForge version published successfully![/green]")
                else:
                    print("[red]Failed to publish NeoForge version.[/red]")
            except Exception as e:
                print(
                    f"[red]Exception occurred while publishing NeoForge version: {e}[/red]"
                )
            finally:
                confirm = typer.confirm("Do you want to continue?", default=True)
                if not confirm:
                    print("[yellow]Publish cancelled.[/yellow]")
                    raise typer.Exit()

        # Update modinfo on Modrinth
        print("[blue]Updating modinfo on Modrinth...[/blue]")
        if not confirm:
            print("[yellow]Update cancelled.[/yellow]")
            raise typer.Exit()
        if modinfo:
            try:
                response = upload_modinfo_to_modrinth(modinfo)
                print("[green]Mod info updated successfully![/green]")
            except Exception as e:
                print(f"[red]Exception occurred while updating mod info: {e}[/red]")

    print("[green]Publish process completed![/green]")

    # Generate Discord announcement from template
    template_path = ROOT_PATH / "scripts" / "templates" / "discord.md"
    if template_path.exists():
        with open(template_path, "r") as f:
            template = f.read()
        announcement = (
            template.replace("%mod_version%", mod_properties.mod_version)
            .replace(
                "%mod_changelog%", changelog if changelog else "No changelog available."
            )
            .replace("%mod_version_type%", mod_properties.mod_version_type)
        )
        announcement_path = ROOT_PATH / "DISCORD.md"
        with open(announcement_path, "w") as f:
            f.write(announcement)
        print(f"[blue]Discord announcement generated at {announcement_path}[/blue]")


if __name__ == "__main__":
    # Load environment variables
    load_env()
    # Run the Typer app
    app()
