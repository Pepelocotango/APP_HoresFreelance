#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Script per concatenar tots els fitxers de codi font del projecte
en un únic fitxer de text estructurat, pensat per a la revisió
per part d'una intel·ligència artificial directora.
"""

import os
import sys

# Nom del fitxer de sortida per defecte
FITXER_SORTIDA = "codi_complet.txt"

# Directoris a ignorar completament
DIRECTORIS_IGNORATS = {
    '.git',
    '.gradle',
    '.idea',
    'node_modules',
    'build',
    'dist',
    'temporals',
    'gradle',
    'dist-electron',
    '__pycache__'
}

# Fitxers específics a ignorar
FITXERS_IGNORATS = {
    'package-lock.json',
    'pnpm-lock.yaml',
    'gradlew',
    'gradlew.bat',
    'gradle-wrapper.jar',
    FITXER_SORTIDA,
    '.gitignore'
}

# Extensions permeses per a la concatenació
EXTENSIONS_PERMESES = {
    '.kt', '.kts', '.tsx', '.ts', '.html', '.css', '.json',
    '.xml', '.properties', '.md', '.py', '.txt', '.pro', '.yml', '.yaml',
    '.svg'
}

def és_fitxer_text_codi(ruta_fitxer):
    """
    Determina si un fitxer s'ha d'incloure en la concatenació basant-se en
    la seva extensió i si el seu nom està a la llista d'ignorats.
    """
    nom_fitxer = os.path.basename(ruta_fitxer)
    if nom_fitxer in FITXERS_IGNORATS:
        return False
        
    _, extensio = os.path.splitext(nom_fitxer)
    if extensio.lower() in EXTENSIONS_PERMESES:
        return True
        
    return False

def concatenar_projecte(directori_arrel, fitxer_sortida_ruta):
    """
    Recorre el projecte de manera recursiva i concatena els fitxers de codi.
    """
    comptador_fitxers = 0
    
    try:
        with open(fitxer_sortida_ruta, 'w', encoding='utf-8') as fs:
            fs.write("========================================================================\n")
            fs.write("CODI COMPLET DEL PROJECTE PER A LA REVISIÓ DE LA IA DIRECTORA\n")
            fs.write(f"Directori d'origen: {os.path.abspath(directori_arrel)}\n")
            fs.write("========================================================================\n\n")
            
            for arrel, directoris, fitxers in os.walk(directori_arrel):
                # Modifiquem directoris in-place per evitar recórrer directoris ignorats
                directoris[:] = [d for d in directoris if d not in DIRECTORIS_IGNORATS]
                
                for fitxer in fitxers:
                    ruta_completa = os.path.join(arrel, fitxer)
                    ruta_relativa = os.path.relpath(ruta_completa, directori_arrel)
                    
                    if és_fitxer_text_codi(ruta_completa):
                        try:
                            # Intentem llegir el contingut del fitxer en UTF-8
                            with open(ruta_completa, 'r', encoding='utf-8', errors='ignore') as fe:
                                contingut = fe.read()
                                
                            fs.write("=" * 80 + "\n")
                            fs.write(f"FITXER: {ruta_relativa}\n")
                            fs.write("=" * 80 + "\n\n")
                            fs.write(contingut)
                            fs.write("\n\n")
                            comptador_fitxers += 1
                            print(f"[Afegit] {ruta_relativa}")
                        except Exception as e:
                            print(f"[Error llegint] {ruta_relativa}: {str(e)}", file=sys.stderr)
                            
        print("\n" + "=" * 50)
        print(f"Concatenació completada correctament!")
        print(f"S'han concatenat {comptador_fitxers} fitxers.")
        print(f"Resultat desat a: {os.path.abspath(fitxer_sortida_ruta)}")
        print("=" * 50)
        
    except Exception as e:
        print(f"Error general en el procés de concatenació: {str(e)}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    # Permet definir un directori arrel o fitxer de sortida alternatius des de la línia de comandes
    dir_arrel = sys.argv[1] if len(sys.argv) > 1 else "."
    fitxer_sortida = sys.argv[2] if len(sys.argv) > 2 else FITXER_SORTIDA
    
    # Comprovem que el directori d'origen existeix
    if not os.path.isdir(dir_arrel):
        print(f"Error: El directori '{dir_arrel}' no existeix.", file=sys.stderr)
        sys.exit(1)
        
    concatenar_projecte(dir_arrel, fitxer_sortida)
