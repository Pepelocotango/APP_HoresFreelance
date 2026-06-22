import os

def generate_codebase():
    exclude_dirs = {
        '.git', '.gradle', 'build', '.idea', 'gradle', 'logo', 'app', 'androidApp/build', 'desktopApp/build', 'shared/build'
    }
    exclude_files = {
        'monorepo_codebase_full.txt', 'concat_codebase.py', 'gradlew', 'gradlew.bat', 'gradle-wrapper.properties'
    }
    allowed_extensions = {
        '.kt', '.kts', '.md', '.properties', '.pro', '.xml'
    }

    output_file = 'monorepo_codebase_full.txt'
    
    # Recollir i ordenar fitxers per tenir un ordre coherent i predictible
    files_to_concat = []
    
    for root, dirs, files in os.walk('.'):
        # Filtrar directoris que cal excloure
        dirs[:] = [d for d in dirs if d not in exclude_dirs and not any(root.endswith(os.sep + ex) or (os.sep + ex + os.sep) in root for ex in exclude_dirs)]
        
        for file in files:
            if file in exclude_files:
                continue
                
            file_path = os.path.join(root, file)
            # Normalitzar camí per utilitzar barres inclinades endavant (estil Unix)
            rel_path = os.path.relpath(file_path, '.').replace('\\', '/')
            if not rel_path.startswith('./'):
                rel_path = './' + rel_path
                
            ext = os.path.splitext(file)[1]
            if ext in allowed_extensions:
                files_to_concat.append(rel_path)

    # Ordenar alfabèticament per mantenir ordre determinista
    files_to_concat.sort()

    with open(output_file, 'w', encoding='utf-8') as outfile:
        for filepath in files_to_concat:
            try:
                # Comprovar si es pot llegir com a text utf-8
                with open(filepath, 'r', encoding='utf-8') as infile:
                    content = infile.read()
                
                outfile.write(f"--- FILE: {filepath} ---\n")
                outfile.write(content)
                if not content.endswith('\n'):
                    outfile.write('\n')
                outfile.write('\n')
                print(f"Afegit: {filepath}")
            except Exception as e:
                print(f"Error llegint {filepath}: {e}")

if __name__ == '__main__':
    generate_codebase()
