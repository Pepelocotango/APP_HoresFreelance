import sqlite3
import json
import uuid
import datetime

def convert_db_to_json(db_path):
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()

    # Mapeig d'IDs antics a nous UUIDs
    client_uuid_map = {}
    
    # 1. Clients
    cursor.execute("SELECT * FROM clients")
    clients = []
    for row in cursor.fetchall():
        old_id = row['id']
        new_uuid = str(uuid.uuid4())
        client_uuid_map[old_id] = new_uuid
        clients.append({
            "id": new_uuid,
            "nom": row['nom'].strip(),
            "preuHoraDefecte": float(row['preuHoraDefecte'])
        })

    # 2. Dies
    cursor.execute("SELECT * FROM dies")
    dies_raw = cursor.fetchall()
    
    # 3. Conceptes
    cursor.execute("SELECT * FROM conceptes")
    conceptes_raw = cursor.fetchall()
    
    # 4. Rangs Horaris
    cursor.execute("SELECT * FROM rangs_horaris")
    rangs_raw = cursor.fetchall()

    final_dies = []
    for dia in dies_raw:
        dia_id = str(uuid.uuid4())
        
        # Convertir data (assumeixo Epoch Day com a Room)
        # Si 'data' és un integer tipus 20620 (dias des de 1970)
        data_obj = datetime.date.fromordinal(dia['data'] + 719163)
        data_str = data_obj.strftime("%Y-%m-%d")
        
        # Filtrar conceptes d'aquest dia
        dia_conceptes = [c for c in conceptes_raw if c['diaId'] == dia['id']]
        final_conceptes = []
        
        for concepte in dia_conceptes:
            concepte_id = str(uuid.uuid4())
            
            # Filtrar rangs d'aquest concepte
            concepte_rangs = [r for r in rangs_raw if r['concepteId'] == concepte['id']]
            final_rangs = []
            for r in concepte_rangs:
                # Convertir segons a HH:mm
                inici_str = (datetime.datetime.min + datetime.timedelta(seconds=r['horaInici'])).strftime("%H:%M")
                fi_str = (datetime.datetime.min + datetime.timedelta(seconds=r['horaFi'])).strftime("%H:%M")
                
                final_rangs.append({
                    "id": str(uuid.uuid4()),
                    "concepteId": concepte_id,
                    "horaInici": inici_str,
                    "horaFi": fi_str
                })
                
            final_conceptes.append({
                "id": concepte_id,
                "diaId": dia_id,
                "nom": concepte['nom'].strip(),
                "preuHora": float(concepte['preuHora']),
                "clientId": client_uuid_map.get(concepte['clientId']),
                "clientNom": None,
                "rangsHoraris": final_rangs,
                "estat": concepte['estat'] if concepte['estat'] else "PENDENT",
                "despeses": float(concepte['despeses']),
                "despesesNotes": concepte['despesesNotes'] if concepte['despesesNotes'] else "",
                "preuFix": False,
                "importFix": 0.0
            })

        final_dies.append({
            "id": dia_id,
            "data": data_str,
            "notes": dia['notes'] if dia['notes'] else "",
            "conceptes": final_conceptes
        })

    return {"clients": clients, "dies": final_dies}

data = convert_db_to_json('BKP_BASE DADES/hores_backup.db')
with open('backup_convertit.json', 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print("Conversió finalitzada: backup_convertit.json")
