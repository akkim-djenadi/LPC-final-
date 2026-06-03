<?php
// backoffice/google_places_sync.php
// Enable explicit error reporting for diagnostics
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

$commerces_file = 'data/commerces.geojson';

// Helper function to extract a Point array [lng, lat] from any GeoJSON geometry type
if (!function_exists('get_point_coordinates')) {
    function get_point_coordinates($geometry) {
        $coords = isset($geometry['coordinates']) ? $geometry['coordinates'] : null;
        if (empty($coords)) {
            return [3.8794, 43.6085];
        }
        
        $find_first_point = function($arr) use (&$find_first_point) {
            if (!is_array($arr)) return null;
            if (count($arr) >= 2 && is_numeric($arr[0]) && is_numeric($arr[1])) {
                return [$arr[0], $arr[1]];
            }
            foreach ($arr as $el) {
                $p = $find_first_point($el);
                if ($p !== null) return $p;
            }
            return null;
        };
        
        $p = $find_first_point($coords);
        return ($p !== null) ? $p : [3.8794, 43.6085];
    }
}

// Helper function to extract Google Maps API Key from GeoJSON
function get_extracted_api_key($file_path) {
    if (file_exists($file_path)) {
        $content = file_get_contents($file_path);
        if (preg_match('/&key=([A-Za-z0-9_-]+)/', $content, $matches)) {
            return $matches[1];
        }
    }
    return '';
}

$default_api_key = get_extracted_api_key($commerces_file);

// Handle AJAX Request (Search / Details)
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action'])) {
    header('Content-Type: application/json; charset=utf-8');
    
    // 1. QUERY GOOGLE PLACES API FOR A MERCHANT
    if ($_POST['action'] === 'fetch_google_details') {
        $merchant_id = $_POST['merchant_id'] ?? '';
        $name = trim($_POST['name'] ?? '');
        $lat = floatval($_POST['lat'] ?? 43.6085);
        $lng = floatval($_POST['lng'] ?? 3.8794);
        $custom_key = trim($_POST['api_key'] ?? '');
        
        $apiKey = !empty($custom_key) ? $custom_key : $default_api_key;
        
        if (empty($apiKey)) {
            echo json_encode(['success' => false, 'error' => "Clé API Google Places absente. Utilisez le champ en haut pour l'introduire."]);
            exit;
        }
        
        if (empty($name)) {
            echo json_encode(['success' => false, 'error' => 'Nom de commerce manquant.']);
            exit;
        }
        
        // Step A: Search for Place ID via Text Search
        // We target Montpellier specifically to avoid finding same-named shops in other cities
        $searchQuery = urlencode($name . ' Montpellier');
        $searchUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json?query={$searchQuery}&key={$apiKey}";
        
        $searchCh = curl_init();
        curl_setopt($searchCh, CURLOPT_URL, $searchUrl);
        curl_setopt($searchCh, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($searchCh, CURLOPT_SSL_VERIFYPEER, false);
        $searchResult = curl_exec($searchCh);
        curl_close($searchCh);
        
        if (!$searchResult) {
            echo json_encode(['success' => false, 'error' => 'Impossible de contacter l\'API de recherche Google.']);
            exit;
        }
        
        $searchData = json_decode($searchResult, true);
        if (($searchData['status'] ?? '') !== 'OK' || empty($searchData['results'])) {
            echo json_encode([
                'success' => false, 
                'error' => 'Aucun résultat trouvé sur Google Maps pour ce nom. (Statut API : ' . ($searchData['status'] ?? 'UNKNOWN') . ')'
            ]);
            exit;
        }
        
        // Take the closest candidate
        $candidate = $searchData['results'][0];
        $placeId = $candidate['place_id'];
        
        // Step B: Query Place Details to fetch editorial summary (description) & business status
        $detailsUrl = "https://maps.googleapis.com/maps/api/place/details/json?place_id={$placeId}&fields=name,editorial_summary,business_status,formatted_phone_number&language=fr&key={$apiKey}";
        
        $detailsCh = curl_init();
        curl_setopt($detailsCh, CURLOPT_URL, $detailsUrl);
        curl_setopt($detailsCh, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($detailsCh, CURLOPT_SSL_VERIFYPEER, false);
        $detailsResult = curl_exec($detailsCh);
        curl_close($detailsCh);
        
        if (!$detailsResult) {
            echo json_encode(['success' => false, 'error' => 'Impossible d\'obtenir les détails de la fiche Google.']);
            exit;
        }
        
        $detailsData = json_decode($detailsResult, true);
        if (($detailsData['status'] ?? '') !== 'OK' || empty($detailsData['result'])) {
            echo json_encode(['success' => false, 'error' => 'Échec de récupération des détails Google Place.']);
            exit;
        }
        
        $result = $detailsData['result'];
        
        $googleName = $result['name'] ?? $name;
        $businessStatus = $result['business_status'] ?? 'OPERATIONAL';
        $googlePhone = $result['formatted_phone_number'] ?? '';
        
        // Retrieve editorial summary if exists
        $editorialSummary = '';
        if (isset($result['editorial_summary']['overview'])) {
            $editorialSummary = $result['editorial_summary']['overview'];
        }
        
        echo json_encode([
            'success' => true,
            'place_id' => $placeId,
            'google_name' => $googleName,
            'business_status' => $businessStatus,
            'description' => $editorialSummary,
            'phone' => $googlePhone
        ], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
        exit;
    }
    
    // 2. APPLY CHANGES BACK TO GEOJSON FILE
    if ($_POST['action'] === 'apply_google_details') {
        $merchant_id = $_POST['merchant_id'] ?? '';
        $new_description = trim($_POST['description'] ?? '');
        $status_action = $_POST['status_action'] ?? 'keep'; // 'keep', 'mark_closed', 'delete'
        $sync_phone = ($_POST['sync_phone'] ?? '0') === '1';
        $google_phone = trim($_POST['phone'] ?? '');
        
        if (empty($merchant_id)) {
            echo json_encode(['success' => false, 'error' => 'Commerce non identifié.']);
            exit;
        }
        
        if (!file_exists($commerces_file)) {
            echo json_encode(['success' => false, 'error' => 'Base commerces.geojson introuvable.']);
            exit;
        }
        
        $geojson = json_decode(file_get_contents($commerces_file), true);
        if (!$geojson || !isset($geojson['features'])) {
            echo json_encode(['success' => false, 'error' => 'Le fichier GeoJSON n\'est pas lisible ou est corrompu.']);
            exit;
        }
        
        $found = false;
        $idx_to_remove = -1;
        
        foreach ($geojson['features'] as $idx => $feat) {
            if (($feat['properties']['id'] ?? '') === $merchant_id) {
                if ($status_action === 'delete') {
                    $idx_to_remove = $idx;
                } else {
                    // Update description if provided and not empty
                    if (!empty($new_description)) {
                        $geojson['features'][$idx]['properties']['description'] = $new_description;
                    }
                    
                    // Update phone if checked
                    if ($sync_phone && !empty($google_phone)) {
                        $geojson['features'][$idx]['properties']['phone'] = $google_phone;
                    }
                    
                    // Handle closed status
                    if ($status_action === 'mark_closed') {
                        $geojson['features'][$idx]['properties']['permanently_closed'] = true;
                        
                        // Let's add an explicit marker inside subcategory if not already there
                        $subcat = $geojson['features'][$idx]['properties']['subcategory'] ?? '';
                        if (strpos($subcat, '[Fermé définitivement]') === false) {
                            $geojson['features'][$idx]['properties']['subcategory'] = ($subcat ? $subcat . ' ' : '') . '🔴 [Fermé définitivement]';
                        }
                    } else {
                        // Reset if we mark as operational/keep
                        unset($geojson['features'][$idx]['properties']['permanently_closed']);
                        $subcat = $geojson['features'][$idx]['properties']['subcategory'] ?? '';
                        $subcat_clean = str_replace(['🔴 [Fermé définitivement]', '[Fermé définitivement]'], '', $subcat);
                        $geojson['features'][$idx]['properties']['subcategory'] = trim($subcat_clean);
                    }
                }
                $found = true;
                break;
            }
        }
        
        if (!$found) {
            echo json_encode(['success' => false, 'error' => 'Commerce introuvable dans la base locale.']);
            exit;
        }
        
        if ($idx_to_remove !== -1) {
            array_splice($geojson['features'], $idx_to_remove, 1);
            $msg = "🗑️ Le commerce a été supprimé de la base de données locale !";
        } else {
            $msg = "✅ Heureux ! Synchronisation enregistrée avec succès !";
        }
        
        file_put_contents($commerces_file, json_encode($geojson, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
        
        echo json_encode(['success' => true, 'message' => $msg]);
        exit;
    }
}

// Read GeoJSON features to render in the table
$commerces = [];
if (file_exists($commerces_file)) {
    $geojson = json_decode(file_get_contents($commerces_file), true);
    if ($geojson && isset($geojson['features'])) {
        $commerces = $geojson['features'];
    }
}

// Include header of PHP backoffice
include 'header.php';
?>

<!-- Introduction -->
<div class="mb-8">
    <h2 class="text-2xl font-bold tracking-tight text-slate-900">Synchroniseur Google Places API</h2>
    <p class="text-xs text-slate-500">Mettez à jour les descriptions de vos établissements et gérez les fermetures définitives en interrogeant directement Google Maps, de manière unitaire et contrôlée.</p>
</div>

<!-- API KEY CONFIG CARD -->
<div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm mb-8">
    <h3 class="text-sm font-bold text-slate-900 mb-2 flex items-center space-x-2">
        <span class="material-symbols-rounded text-orange-500">vpn_key</span>
        <span>Clé API Google Places</span>
    </h3>
    <p class="text-xs text-slate-500 mb-4">
        Nous extrayons par défaut la clé API active de vos commerces. Vous pouvez la surcharger ci-dessous si nécessaire.
    </p>
    <div class="flex items-center space-x-3 max-w-xl">
        <input type="password" id="api_key_override" class="flex-1 px-3 py-2 border border-slate-200 rounded-xl text-xs font-mono focus:outline-none focus:border-orange-500" 
               placeholder="Clé API Google Places (AIzaSy...)" value="<?php echo htmlspecialchars($default_api_key); ?>">
        <button onclick="toggleVisibility('api_key_override')" class="px-3 py-2 bg-slate-100 hover:bg-slate-200 rounded-xl text-xs text-slate-700 font-semibold transition">
            Afficher/Masquer
        </button>
    </div>
</div>

<!-- BULK SINK / PROGRESS INDICATOR -->
<div class="bg-gradient-to-r from-orange-500 to-amber-500 p-6 rounded-2xl text-white shadow-sm mb-8 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
    <div>
        <h3 class="text-base font-bold flex items-center space-x-2">
            <span class="material-symbols-rounded">rocket_launch</span>
            <span>Vérification progressive des commerces</span>
        </h3>
        <p class="text-xs text-white/90 mt-1 max-w-xl">Vous pouvez effectuer des vérifications successives pour chacun de vos commerces de manière contrôlée sans faire exploser l'usage ou les quotas de votre clé Google.</p>
    </div>
    <div class="flex space-x-2">
        <button id="btn_run_bulk" onclick="startBulkCheck()" class="px-4 py-2 bg-white text-orange-600 rounded-xl font-bold text-xs shadow hover:bg-orange-50 transition flex items-center space-x-1.5">
            <span class="material-symbols-rounded text-sm">play_circle</span>
            <span>Vérifier en boucle le lot</span>
        </button>
        <button id="btn_stop_bulk" onclick="stopBulkCheck()" class="hidden px-4 py-2 bg-rose-600 text-white rounded-xl font-bold text-xs shadow hover:bg-rose-700 transition flex items-center space-x-1.5">
            <span class="material-symbols-rounded text-sm">stop_circle</span>
            <span>Arrêter</span>
        </button>
    </div>
</div>

<!-- BULK STATUS CONTAINER -->
<div id="bulk_progress_card" class="hidden bg-white p-5 rounded-2xl border border-slate-200 shadow-sm mb-8 space-y-3">
    <div class="flex justify-between items-center text-xs">
        <span class="font-bold text-slate-700" id="progress_label">Vérification en cours : 0%</span>
        <span class="font-mono text-slate-400" id="progress_counts">0 / 0 commerces</span>
    </div>
    <div class="w-full bg-slate-100 h-2.5 rounded-full overflow-hidden">
        <div id="progress_bar" class="bg-orange-500 h-full w-0 transition-all duration-300"></div>
    </div>
</div>

<!-- COMMERCE LIST FOR MANAGE -->
<div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
    <!-- Local search and filter tools -->
    <div class="p-5 border-b border-slate-200 bg-slate-50/50 flex flex-col sm:flex-row gap-4 items-center justify-between">
        <div class="flex items-center space-x-3 w-full sm:w-auto">
            <span class="text-sm font-bold text-slate-800">Établissements Éligibles (<?php echo count($commerces); ?>)</span>
        </div>
        <div class="flex gap-2 w-full sm:w-auto">
            <input type="text" id="sync_search_input" onkeyup="filterSyncTable()" placeholder="Rechercher commerce..." 
                   class="px-3 py-2 border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-1 focus:ring-orange-500 bg-white">
        </div>
    </div>

    <?php if (!empty($commerces)): ?>
        <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse" id="sync_commerces_table">
                <thead>
                    <tr class="border-b border-slate-200 text-[10px] font-bold text-slate-400 uppercase tracking-wider bg-slate-50">
                        <th class="py-3 px-4 w-12 text-center">
                            <input type="checkbox" id="sync_master_cb" checked onclick="toggleAllSyncCheckboxes(this)" class="rounded border-slate-300 text-orange-500 h-4 w-4 cursor-pointer">
                        </th>
                        <th class="py-3 px-4">Établissement</th>
                        <th class="py-3 px-4 max-w-xs">Description Actuelle</th>
                        <th class="py-3 px-4">Données Google Place API</th>
                        <th class="py-3 px-4 text-right">Actions</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100 text-xs">
                    <?php foreach ($commerces as $idx => $feature):
                        $props = $feature['properties'];
                        $id = $props['id'] ?? '';
                        $coords = get_point_coordinates($feature['geometry'] ?? []);
                        $is_closed_local = isset($props['permanently_closed']) || (strpos($props['subcategory'] ?? '', '[Fermé définitivement]') !== false);
                    ?>
                        <tr class="sync-row hover:bg-slate-50/50 transition duration-150" 
                            id="row_<?php echo htmlspecialchars($id); ?>"
                            data-name="<?php echo htmlspecialchars($props['name'] ?? ''); ?>"
                            data-lat="<?php echo $coords[1]; ?>"
                            data-lng="<?php echo $coords[0]; ?>">
                            
                            <td class="py-4 px-4 text-center">
                                <input type="checkbox" value="<?php echo htmlspecialchars($id); ?>" 
                                       class="sync-merchant-checkbox rounded border-slate-300 text-orange-500 h-4 w-4 cursor-pointer" checked>
                            </td>
                            
                            <td class="py-4 px-4 font-semibold text-slate-800">
                                <div>
                                    <span><?php echo htmlspecialchars($props['name'] ?? 'Inconnu'); ?></span>
                                    <?php if ($is_closed_local): ?>
                                        <span class="ml-1 text-[9px] px-1 py-0.2 rounded bg-rose-100 text-rose-700 font-bold uppercase">Locale: Fermé 🔴</span>
                                    <?php endif; ?>
                                    <p class="text-[10px] text-slate-400 font-mono font-normal mt-0.5"><?php echo htmlspecialchars($id); ?></p>
                                </div>
                            </td>
                            
                            <td class="py-4 px-4 text-slate-500 max-w-xs leading-relaxed" id="local_desc_<?php echo htmlspecialchars($id); ?>">
                                <?php echo htmlspecialchars($props['description'] ?? 'Aucune description présente.'); ?>
                            </td>
                            
                            <td class="py-4 px-4" id="google_data_<?php echo htmlspecialchars($id); ?>">
                                <span class="text-slate-400 italic font-medium">En attente de contrôle...</span>
                            </td>
                            
                            <td class="py-4 px-4 text-right">
                                <div class="flex items-center justify-end space-x-2" id="controls_<?php echo htmlspecialchars($id); ?>">
                                    <button onclick="checkOnGoogle('<?php echo htmlspecialchars($id); ?>')" 
                                            class="px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-[11px] rounded-lg transition inline-flex items-center space-x-1">
                                        <span class="material-symbols-rounded text-sm">search</span>
                                        <span>Consulter Google</span>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    <?php else: ?>
        <div class="p-8 text-center text-slate-400 italic block">
            Aucun commerce n'est enregistré dans votre base commerces.geojson.
        </div>
    <?php endif; ?>
</div>

<script>
let bulkRunning = false;
let bulkIndex = 0;
let bulkQueue = [];

function toggleVisibility(id) {
    const el = document.getElementById(id);
    if (el.type === "password") {
        el.type = "text";
    } else {
        el.type = "password";
    }
}

function toggleAllSyncCheckboxes(master_cb) {
    const checked = master_cb.checked;
    document.querySelectorAll('.sync-merchant-checkbox').forEach(cb => {
        cb.checked = checked;
    });
}

function filterSyncTable() {
    const query = document.getElementById('sync_search_input').value.toLowerCase();
    document.querySelectorAll('.sync-row').forEach(row => {
        const name = row.dataset.name.toLowerCase();
        if (name.includes(query)) {
            row.classList.remove('hidden');
        } else {
            row.classList.add('hidden');
        }
    });
}

// Check a single commerce using Google Places API
function checkOnGoogle(id) {
    const row = document.getElementById('row_' + id);
    if (!row) return;
    
    const name = row.dataset.name;
    const lat = row.dataset.lat;
    const lng = row.dataset.lng;
    const apiKey = document.getElementById('api_key_override').value;
    
    const controls = document.getElementById('controls_' + id);
    const googleDataCell = document.getElementById('google_data_' + id);
    
    // UI Loading state
    googleDataCell.innerHTML = `
        <div class="flex items-center space-x-1 text-orange-500 font-semibold animate-pulse">
            <span class="material-symbols-rounded animate-spin text-sm">progress_activity</span>
            <span>Interrogation Google...</span>
        </div>
    `;
    
    const params = new URLSearchParams();
    params.append('action', 'fetch_google_details');
    params.append('merchant_id', id);
    params.append('name', name);
    params.append('lat', lat);
    params.append('lng', lng);
    params.append('api_key', apiKey);
    
    fetch('google_places_sync.php', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    })
    .then(response => response.json())
    .then(data => {
        if (!data.success) {
            googleDataCell.innerHTML = `
                <div class="p-2 rounded bg-rose-50 border border-rose-200 text-rose-700 text-[11px] font-semibold flex items-center space-x-1">
                    <span class="material-symbols-rounded text-sm">warning</span>
                    <span>${data.error}</span>
                </div>
            `;
            return;
        }
        
        renderGoogleSelection(id, data);
    })
    .catch(err => {
        googleDataCell.innerHTML = `
            <div class="p-2 rounded bg-rose-50 border border-rose-200 text-rose-700 text-[11px] font-semibold">
                Erreur de réseau ou de communication.
            </div>
        `;
    });
}

// Display Google details retrieved, allowing manual choosing of action
function renderGoogleSelection(id, googleData) {
    const googleDataCell = document.getElementById('google_data_' + id);
    const controls = document.getElementById('controls_' + id);
    
    let statusBadge = '';
    let statusActionDefault = 'keep';
    
    if (googleData.business_status === 'CLOSED_PERMANENTLY') {
        statusBadge = '<span class="px-1.5 py-0.5 rounded bg-rose-100 text-rose-700 font-extrabold uppercase text-[10px]">⚠️ Google: FERMÉ DÉFINITIVEMENT 🔴</span>';
        statusActionDefault = 'mark_closed';
    } else if (googleData.business_status === 'CLOSED_TEMPORARILY') {
        statusBadge = '<span class="px-1.5 py-0.5 rounded bg-amber-100 text-amber-700 font-bold uppercase text-[10px]">⚠️ Google: Fermé temporairement 🟡</span>';
        statusActionDefault = 'keep';
    } else {
        statusBadge = '<span class="px-1.5 py-0.5 rounded bg-emerald-100 text-emerald-700 font-bold uppercase text-[10px]">Google: Ouvert 🟢</span>';
    }
    
    const googleDescription = googleData.description ? googleData.description : 'Aucune description fournie par Google Maps.';
    const displayPhone = googleData.phone ? `Tél Google: ${googleData.phone}` : 'Pas de n° de tél sur Google';
    
    googleDataCell.innerHTML = `
        <div class="space-y-2 p-3 bg-slate-50 border border-slate-200 rounded-xl">
            <div class="flex flex-wrap items-center gap-2">
                <span class="font-bold text-slate-800 text-[11px]">${googleData.google_name}</span>
                ${statusBadge}
            </div>
            
            <p class="text-slate-600 block italic leading-snug">${googleDescription}</p>
            <p class="text-[10px] font-mono text-slate-400 font-normal">${displayPhone}</p>
            
            <div class="pt-2 border-t border-slate-100 flex flex-wrap gap-x-4 gap-y-1.5 items-center">
                <!-- Sync Actions Selection -->
                <div class="flex items-center space-x-2">
                    <span class="font-semibold text-slate-500 text-[10px]">Action statut:</span>
                    <select id="action_sel_${id}" class="px-2 py-0.5 border border-slate-200 rounded text-[10px] bg-white font-semibold">
                        <option value="keep" ${statusActionDefault === 'keep' ? 'selected' : ''}>Conserver ouvert (Normal)</option>
                        <option value="mark_closed" ${statusActionDefault === 'mark_closed' ? 'selected' : ''}>Étiqueter Fermé définitivement</option>
                        <option value="delete" ${statusActionDefault === 'delete' ? 'selected' : ''}>Détruire / Supprimer commerce de la base</option>
                    </select>
                </div>
                
                <?php if (!empty($googlePhone)): ?>
                <label class="flex items-center space-x-1 text-[10px] font-semibold text-slate-500 cursor-pointer">
                    <input type="checkbox" id="phone_cb_${id}" checked class="rounded border-slate-300 text-orange-500">
                    <span>Synchroniser le Téléphone</span>
                </label>
                <?php endif; ?>
            </div>
        </div>
    `;
    
    controls.innerHTML = `
        <button onclick="applyGoogleDetails('${id}', \`${encodeURIComponent(googleData.description)}\`, \`${googleData.phone}\`)"
                class="px-3 py-1.5 bg-orange-500 hover:bg-orange-600 font-bold text-white text-[11px] rounded-lg shadow-sm transition inline-flex items-center space-x-1">
            <span class="material-symbols-rounded text-sm">save</span>
            <span>Appliquer et Enregistrer</span>
        </button>
    `;
}

// Post changes back to server
function applyGoogleDetails(id, encodedDesc, phone) {
    const row = document.getElementById('row_' + id);
    const selectAction = document.getElementById('action_sel_' + id);
    const actionVal = selectAction ? selectAction.value : 'keep';
    
    const phoneCb = document.getElementById('phone_cb_' + id);
    const syncPhoneVal = phoneCb && phoneCb.checked ? '1' : '0';
    
    const descriptionText = decodeURIComponent(encodedDesc);
    
    const controls = document.getElementById('controls_' + id);
    controls.innerHTML = `<span class="text-slate-400 font-medium animate-pulse text-[11px]">Enregistrement...</span>`;
    
    const params = new URLSearchParams();
    params.append('action', 'apply_google_details');
    params.append('merchant_id', id);
    params.append('description', descriptionText);
    params.append('status_action', actionVal);
    params.append('sync_phone', syncPhoneVal);
    params.append('phone', phone);
    
    fetch('google_places_sync.php', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    })
    .then(response => response.json())
    .then(data => {
        if (!data.success) {
            alert("Erreur: " + data.error);
            controls.innerHTML = `<span class="text-rose-600 font-bold text-[11px]">Échec</span>`;
            return;
        }
        
        // Handle physical removal from DOM if deleted
        if (actionVal === 'delete') {
            row.remove();
        } else {
            // Update UI values locally
            const localDescCell = document.getElementById('local_desc_' + id);
            if (descriptionText.trim() !== "") {
                localDescCell.innerHTML = descriptionText;
                localDescCell.classList.add('text-emerald-700', 'font-semibold');
            }
            
            const googleDataCell = document.getElementById('google_data_' + id);
            googleDataCell.innerHTML = `
                <div class="p-2 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded font-semibold text-[10px] inline-flex items-center space-x-1">
                    <span class="material-symbols-rounded text-xs">check_circle</span>
                    <span>Modifié & Synchronisé localement</span>
                </div>
            `;
            
            controls.innerHTML = `
                <span class="text-emerald-600 font-bold text-[11px] flex items-center">
                    <span class="material-symbols-rounded text-sm mr-0.5">done</span> Synchronisé
                </span>
            `;
        }
    })
    .catch(err => {
        alert("Erreur réseau");
    });
}

// Progressive sequential bulk checker
function startBulkCheck() {
    if (bulkRunning) return;
    
    // Grab all checked merchants
    bulkQueue = [];
    document.querySelectorAll('.sync-merchant-checkbox:checked').forEach(cb => {
        bulkQueue.push(cb.value);
    });
    
    if (bulkQueue.length === 0) {
        alert("Veuillez cocher au moins un commerce pour la vérification progressive.");
        return;
    }
    
    bulkRunning = true;
    bulkIndex = 0;
    
    document.getElementById('bulk_progress_card').classList.remove('hidden');
    document.getElementById('btn_run_bulk').classList.add('hidden');
    document.getElementById('btn_stop_bulk').classList.remove('hidden');
    
    updateBulkProgressBar();
    runNextBulkStep();
}

function stopBulkCheck() {
    bulkRunning = false;
    document.getElementById('btn_run_bulk').classList.remove('hidden');
    document.getElementById('btn_stop_bulk').classList.add('hidden');
}

function runNextBulkStep() {
    if (!bulkRunning) return;
    if (bulkIndex >= bulkQueue.length) {
        stopBulkCheck();
        document.getElementById('progress_label').innerText = "Vérification progressive terminée !";
        return;
    }
    
    const merchantId = bulkQueue[bulkIndex];
    const row = document.getElementById('row_' + merchantId);
    
    if (row) {
        // Scroll slightly to let the admin watch the magic
        row.scrollIntoView({ behavior: 'smooth', block: 'center' });
        
        // Query Google for this row
        checkOnGoogle(merchantId);
    }
    
    bulkIndex++;
    updateBulkProgressBar();
    
    // Delay 1.5 seconds between queries to avoid hitting immediate rates/limits and respect the user's budget
    setTimeout(runNextBulkStep, 1500);
}

function updateBulkProgressBar() {
    const total = bulkQueue.length;
    const progressPercent = total > 0 ? Math.round((bulkIndex / total) * 100) : 0;
    
    document.getElementById('progress_bar').style.width = progressPercent + '%';
    document.getElementById('progress_label').innerText = `Vérification en cours : ${progressPercent}%`;
    document.getElementById('progress_counts').innerText = `${bulkIndex} / ${total} commerces`;
}
</script>

<?php include 'footer.php'; ?>
