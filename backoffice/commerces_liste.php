<?php
// Enable explicit error reporting for diagnostics
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

// Helper function to extract a Point array [lng, lat] from any GeoJSON geometry type
if (!function_exists('get_point_coordinates')) {
    function get_point_coordinates($geometry) {
        $coords = isset($geometry['coordinates']) ? $geometry['coordinates'] : null;
        if (empty($coords)) {
            return [3.8794, 43.6085];
        }
        
        // Helper function to recursively find the first array of size 2 containing numeric values
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

$commerces_file = 'data/commerces.geojson';
$coupons_file = 'data/coupons.json';

$commerces = [];
$coupons = [];
$feedback_msg = "";
$feedback_type = "success";

// DIRECT GEOJSON IMPORT POST HANDLER
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_FILES['geojson_file'])) {
    if ($_FILES['geojson_file']['error'] === UPLOAD_ERR_OK) {
        $tmp_name = $_FILES['geojson_file']['tmp_name'];
        $content = file_get_contents($tmp_name);
        $decoded = json_decode($content, true);
        
        if ($decoded === null) {
            $feedback_msg = "❌ Le fichier importé n'est pas un JSON valide.";
            $feedback_type = "error";
        } elseif (!isset($decoded['type']) || $decoded['type'] !== 'FeatureCollection') {
            $feedback_msg = "❌ Le fichier doit être une FeatureCollection GeoJSON ou JSON de commerces valide.";
            $feedback_type = "error";
        } else {
            if (file_put_contents($commerces_file, json_encode($decoded, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE))) {
                $feedback_msg = "✅ Succès ! Votre fichier commerces.geojson a été mis à jour directement.";
                $feedback_type = "success";
            } else {
                $feedback_msg = "❌ Une erreur s'est produite lors de la sauvegarde du fichier.";
                $feedback_type = "error";
            }
        }
    } else {
        $feedback_msg = "❌ Erreur de transfert du fichier (code d'erreur : " . $_FILES['geojson_file']['error'] . ")";
        $feedback_type = "error";
    }
}

// Load data files safely (ensures fresh reads of imported files)
if (file_exists($commerces_file)) {
    $geojson = json_decode(file_get_contents($commerces_file), true);
    if ($geojson && isset($geojson['features'])) {
        $features = $geojson['features'];
        $modified = false;
        foreach ($features as $idx => &$feat) {
            if (!isset($feat['properties'])) {
                $feat['properties'] = array();
                $modified = true;
            }
            if (!isset($feat['properties']['id']) || empty($feat['properties']['id'])) {
                if (isset($feat['id']) && !empty($feat['id'])) {
                    $feat['properties']['id'] = $feat['id'];
                } else {
                    $clean_name = strtolower(preg_replace('/[^a-zA-Z0-9]/', '', $feat['properties']['name'] ?? 'com'));
                    $feat['properties']['id'] = 'mer_' . (empty($clean_name) ? 'com' : $clean_name) . '_' . ($idx + 1);
                }
                $modified = true;
            }
        }
        unset($feat);
        $commerces = $features;
        if ($modified) {
            $geojson['features'] = $features;
            file_put_contents($commerces_file, json_encode($geojson, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
        }
    }
}

if (file_exists($coupons_file)) {
    $coupons = json_decode(file_get_contents($coupons_file), true);
}

// POST REQUESTS HANDLER
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    
    // 1. INDIVIDUAL PARTNER EDIT & COUPONS MUTATION
    if (isset($_POST['action']) && $_POST['action'] === 'save_single') {
        $id = $_POST['merchant_id'] ?? '';
        $name = trim($_POST['name'] ?? '');
        $category = trim($_POST['category'] ?? '');
        $subcategory = trim($_POST['subcategory'] ?? '');
        $phone = trim($_POST['phone'] ?? '');
        $description = trim($_POST['description'] ?? '');
        $github_image = trim($_POST['github_image'] ?? '');
        $lat = floatval($_POST['lat'] ?? 43.6085);
        $lng = floatval($_POST['lng'] ?? 3.8794);
        
        $coupon_title = trim($_POST['coupon_title'] ?? '');
        $coupon_desc = trim($_POST['coupon_description'] ?? '');
        $coupon_quota = intval($_POST['coupon_quota'] ?? 0);
        $coupon_active = (isset($_POST['coupon_active']) && $_POST['coupon_active'] == '1') ? true : false;
        
        if (empty($name)) {
            $feedback_msg = "❌ Le nom de l'établissement ne peut pas être vide.";
            $feedback_type = "error";
        } else {
            // Find and modify active commerce
            $found_commerce = false;
            foreach ($commerces as $key => $feature) {
                if (($feature['properties']['id'] ?? '') === $id) {
                    $commerces[$key]['properties']['name'] = $name;
                    $commerces[$key]['properties']['category'] = $category;
                    $commerces[$key]['properties']['subcategory'] = $subcategory;
                    $commerces[$key]['properties']['phone'] = $phone;
                    $commerces[$key]['properties']['description'] = $description;
                    $commerces[$key]['properties']['github_image'] = $github_image;
                    $commerces[$key]['geometry']['coordinates'] = [$lng, $lat];
                    $found_commerce = true;
                    break;
                }
            }
            
            if ($found_commerce) {
                // Save updated geojson back
                $new_geojson = [
                    "type" => "FeatureCollection",
                    "features" => array_values($commerces)
                ];
                file_put_contents($commerces_file, json_encode($new_geojson, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
                
                // Track associated coupon change
                $coupon_found_index = -1;
                foreach ($coupons as $idx => $c) {
                    if (($c['merchant_id'] ?? '') === $id) {
                        $coupon_found_index = $idx;
                        break;
                    }
                }
                
                if (!empty($coupon_title)) {
                    if ($coupon_found_index !== -1) {
                        // Update existing one
                        $coupons[$coupon_found_index]['title'] = $coupon_title;
                        $coupons[$coupon_found_index]['description'] = $coupon_desc;
                        $coupons[$coupon_found_index]['quota'] = $coupon_quota;
                        $coupons[$coupon_found_index]['active'] = $coupon_active;
                    } else {
                        // Create brand new
                        $new_cop = [
                            "id" => "coup_" . rand(1000, 9999),
                            "merchant_id" => $id,
                            "title" => $coupon_title,
                            "description" => $coupon_desc,
                            "quota" => $coupon_quota,
                            "active" => $coupon_active
                        ];
                        $coupons[] = $new_cop;
                    }
                } else {
                    // Remove coupon if title cleared out
                    if ($coupon_found_index !== -1) {
                        unset($coupons[$coupon_found_index]);
                        $coupons = array_values($coupons);
                    }
                }
                
                file_put_contents($coupons_file, json_encode($coupons, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
                $feedback_msg = "✅ L'établissement '$name' et ses coupons rattachés ont été actualisés avec succès !";
                $feedback_type = "success";
            } else {
                $feedback_msg = "❌ Établissement introuvable.";
                $feedback_type = "error";
            }
        }
    }
    
    // 2. BULK / BATCH ACTIONS DECK (mass modifications)
    if (isset($_POST['action']) && $_POST['action'] === 'bulk_save') {
        $selected_ids = $_POST['selected_ids'] ?? [];
        $bulk_action = $_POST['bulk_action'] ?? '';
        
        if (empty($selected_ids)) {
            $feedback_msg = "❌ Aucun établissement n'a été coché pour l'opération de groupe.";
            $feedback_type = "error";
        } else {
            if ($bulk_action === 'delete') {
                // Cascade delete establishments
                $commerces = array_filter($commerces, function($feature) use ($selected_ids) {
                    return !in_array($feature['properties']['id'] ?? '', $selected_ids);
                });
                $commerces = array_values($commerces);
                
                // Erase their linked discount coupons
                $coupons = array_filter($coupons, function($c) use ($selected_ids) {
                    return !in_array($c['merchant_id'] ?? '', $selected_ids);
                });
                $coupons = array_values($coupons);
                
                $new_geojson = [
                    "type" => "FeatureCollection",
                    "features" => $commerces
                ];
                file_put_contents($commerces_file, json_encode($new_geojson, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
                file_put_contents($coupons_file, json_encode($coupons, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
                
                $feedback_msg = "🗑️ Suppression globale accomplie : " . count($selected_ids) . " commerces retirés définitivement.";
                $feedback_type = "success";
                
            } elseif ($bulk_action === 'update') {
                $apply_category = $_POST['apply_category'] ?? '';
                $bulk_cat_val = trim($_POST['bulk_category'] ?? '');
                
                $apply_coupon = $_POST['apply_coupon'] ?? '';
                $bulk_cop_title = trim($_POST['bulk_coupon_title'] ?? '');
                $bulk_cop_desc = trim($_POST['bulk_coupon_description'] ?? '');
                $bulk_cop_quota = intval($_POST['bulk_coupon_quota'] ?? 0);
                $bulk_cop_active = (isset($_POST['bulk_coupon_active']) && $_POST['bulk_coupon_active'] == '1') ? true : false;
                
                $remove_coupons = isset($_POST['bulk_remove_coupons']) && $_POST['bulk_remove_coupons'] == '1';
                
                // Apply mass Categories
                $updated_cnt = 0;
                foreach ($commerces as $key => $feature) {
                    $m_id = $feature['properties']['id'] ?? '';
                    if (in_array($m_id, $selected_ids)) {
                        if ($apply_category === '1' && !empty($bulk_cat_val)) {
                            $commerces[$key]['properties']['category'] = $bulk_cat_val;
                        }
                        $updated_cnt++;
                    }
                }
                
                if ($updated_cnt > 0) {
                    $new_geojson = [
                        "type" => "FeatureCollection",
                        "features" => $commerces
                    ];
                    file_put_contents($commerces_file, json_encode($new_geojson, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
                }
                
                // Apply mass Coupons
                if ($remove_coupons) {
                    $coupons = array_filter($coupons, function($c) use ($selected_ids) {
                        return !in_array($c['merchant_id'] ?? '', $selected_ids);
                    });
                    $coupons = array_values($coupons);
                } elseif ($apply_coupon === '1' && !empty($bulk_cop_title)) {
                    foreach ($selected_ids as $sel_id) {
                        $cop_idx = -1;
                        foreach ($coupons as $idx => $c) {
                            if (($c['merchant_id'] ?? '') === $sel_id) {
                                $cop_idx = $idx;
                                break;
                            }
                        }
                        
                        if ($cop_idx !== -1) {
                            // Override existing
                            $coupons[$cop_idx]['title'] = $bulk_cop_title;
                            $coupons[$cop_idx]['description'] = $bulk_cop_desc;
                            $coupons[$cop_idx]['quota'] = $bulk_cop_quota;
                            $coupons[$cop_idx]['active'] = $bulk_cop_active;
                        } else {
                            // Insert a new coupon
                            $new_c = [
                                "id" => "coup_" . rand(1000, 9999),
                                "merchant_id" => $sel_id,
                                "title" => $bulk_cop_title,
                                "description" => $bulk_cop_desc,
                                "quota" => $bulk_cop_quota,
                                "active" => $bulk_cop_active
                            ];
                            $coupons[] = $new_c;
                        }
                    }
                }
                
                file_put_contents($coupons_file, json_encode($coupons, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
                $feedback_msg = "✅ Modifications de lot terminées avec succès pour les " . count($selected_ids) . " partenaires séléctionnés.";
                $feedback_type = "success";
            }
        }
    }

    // 3. SMART AUTOMATIC CATEGORIZATION HANDLER
    if (isset($_POST['action']) && $_POST['action'] === 'auto_categorize') {
        $overwrite = isset($_POST['overwrite']) && $_POST['overwrite'] === '1';
        $updated_count = 0;
        
        // Define smart classification function
        $classify = function($properties) {
            $cat = "Shopping"; // fallback
            $subcat = "";
            
            $amenity = strtolower($properties['amenity'] ?? '');
            $shop = strtolower($properties['shop'] ?? '');
            $tourism = strtolower($properties['tourism'] ?? '');
            $leisure = strtolower($properties['leisure'] ?? '');
            $cuisine = strtolower($properties['cuisine'] ?? '');
            $sport = strtolower($properties['sport'] ?? '');
            $craft = strtolower($properties['craft'] ?? '');
            
            // Restauration
            if (in_array($amenity, ['restaurant', 'fast_food', 'food_court', 'cafe']) ||
                in_array($shop, ['bakery', 'butcher', 'dairy', 'pastry', 'confectionery'])) {
                $cat = "Restauration";
                if ($shop === 'bakery') $subcat = "Boulangerie 🥖";
                elseif ($shop === 'butcher') $subcat = "Boucherie 🥩";
                elseif ($shop === 'pastry') $subcat = "Pâtisserie 🍰";
                elseif ($shop === 'confectionery') $subcat = "Chocolats & Confiserie 🍫";
                elseif ($amenity === 'cafe') $subcat = "Café & Brunch ☕";
                elseif ($amenity === 'fast_food') {
                    $subcat = !empty($cuisine) ? "Restauration rapide (" . ucfirst($cuisine) . ") 🍔" : "Restauration rapide 🍟";
                } elseif ($amenity === 'restaurant') {
                    $subcat = !empty($cuisine) ? "Restaurant (" . ucfirst($cuisine) . ") 🍽️" : "Restaurant 🍽️";
                }
            }
            // Cave & Bar
            elseif (in_array($amenity, ['bar', 'pub', 'biergarten']) ||
                    in_array($shop, ['alcohol', 'wine', 'beer', 'beverages'])) {
                $cat = "Cave & Bar";
                if ($amenity === 'pub') $subcat = "Pub traditionnel 🍺";
                elseif ($amenity === 'bar') $subcat = "Bar & Cocktails 🍹";
                elseif ($shop === 'wine' || $shop === 'alcohol') $subcat = "Cave à vins & spiritueux 🍷";
                else $subcat = "Bar & Boissons 🥂";
            }
            // Culture
            elseif (in_array($shop, ['books', 'music', 'musical_instrument', 'art']) ||
                    in_array($amenity, ['cinema', 'theatre', 'arts_centre', 'museum', 'library', 'studio'])) {
                $cat = "Culture";
                if ($shop === 'books') $subcat = "Librairie & Livres 📚";
                elseif ($shop === 'musical_instrument') $subcat = "Instruments de musique 🎸";
                elseif ($shop === 'art' || $amenity === 'arts_centre') $subcat = "Galerie d'Art 🎨";
                elseif ($shop === 'music') $subcat = "Disquaire & Musique 📀";
                else $subcat = "Arts & Culture 🎭";
            }
            // Hôtels / Logement
            elseif (in_array($tourism, ['hotel', 'hostel', 'motel', 'guest_house', 'apartment'])) {
                $cat = "Hôtels / Logement";
                $subcat = "Hébergement (" . ucfirst($tourism) . ") 🏨";
            }
            // Loisirs
            elseif (!empty($leisure) || 
                    in_array($shop, ['games', 'video', 'tobacco', 'bicycle']) ||
                    in_array($amenity, ['internet_cafe', 'escape_game']) ||
                    !empty($sport)) {
                $cat = "Loisirs";
                if ($shop === 'games') $subcat = "Jeux & Jouets 🎲";
                elseif ($shop === 'bicycle') $subcat = "Vélos & Randonnée 🚲";
                elseif ($shop === 'tobacco') $subcat = "Tabac, Presse & Kiosque 🚬";
                else $subcat = "Loisirs & Divertissements 🎮";
            }
            // Shopping Fallback
            elseif (!empty($shop) || !empty($craft)) {
                $cat = "Shopping";
                if ($shop === 'convenience' || $shop === 'supermarket' || $shop === 'grocery') {
                    $subcat = "Épicerie & Alimentation 🛒";
                } elseif ($shop === 'clothes') {
                    $subcat = "Prêt-à-porter 👕";
                } elseif ($shop === 'shoes') {
                    $subcat = "Chaussures & Accessoires 👟";
                } elseif ($shop === 'jewelry') {
                    $subcat = "Bijouterie & Joaillerie 💎";
                } elseif ($shop === 'beauty' || $shop === 'cosmetics') {
                    $subcat = "Esthétique, Beauté & Cosmétiques 💅";
                } elseif ($shop === 'toys') {
                    $subcat = "Jeux & Jouets 🧸";
                } elseif ($shop === 'florist') {
                    $subcat = "Fleuriste 🌸";
                } elseif ($shop === 'dry_cleaning' || $shop === 'laundry') {
                    $subcat = "Pressing & Laverie 🧺";
                } elseif ($craft === 'locksmith') {
                    $subcat = "Serrurerie & Dépannage 🔑";
                } elseif ($craft === 'tailor' || $craft === 'sewing') {
                    $subcat = "Couture & Retouches 🧵";
                } else {
                    $subcat = "Boutique (" . str_replace('_', ' ', $shop ? $shop : $craft) . ") 🛍️";
                }
            }
            
            return ['cat' => $cat, 'subcat' => $subcat];
        };
        
        foreach ($commerces as $key => $feature) {
            $props = $feature['properties'] ?? [];
            $existing_cat = $props['category'] ?? '';
            
            // Check if we should categorize this one
            if ($overwrite || empty($existing_cat) || $existing_cat === 'Autre') {
                $result = $classify($props);
                $commerces[$key]['properties']['category'] = $result['cat'];
                // Only overwrite subcategory if it's currently empty
                if (empty($props['subcategory'] ?? '')) {
                    $commerces[$key]['properties']['subcategory'] = $result['subcat'];
                }
                $updated_count++;
            }
        }
        
        if ($updated_count > 0) {
            $new_geojson = [
                "type" => "FeatureCollection",
                "features" => $commerces
            ];
            file_put_contents($commerces_file, json_encode($new_geojson, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            $feedback_msg = "⚡ Succès ! Catégorisation intelligente effectuée pour $updated_count commerces.";
            $feedback_type = "success";
        } else {
            $feedback_msg = "ℹ️ Aucun commerce n'avait besoin d'une suggestion automatique de catégorie.";
            $feedback_type = "success";
        }
    }
    
    // Reaload mutated arrays for safe viewing
    if (file_exists($commerces_file)) {
        $geojson = json_decode(file_get_contents($commerces_file), true);
        if ($geojson && isset($geojson['features'])) {
            $commerces = $geojson['features'];
        }
    }
    if (file_exists($coupons_file)) {
        $coupons = json_decode(file_get_contents($coupons_file), true);
    }
}

// Prepare quick coupon index
$merchant_coupons = [];
foreach ($coupons as $c) {
    if (isset($c['merchant_id'])) {
        $merchant_coupons[$c['merchant_id']] = $c;
    }
}

include 'header.php';
?>

<!-- Alert Feedback -->
<?php if (!empty($feedback_msg)): ?>
    <div id="status_alert" class="auto-dismiss mb-6 p-4 rounded-xl border <?php echo ($feedback_type === 'success') ? 'bg-emerald-50 border-emerald-300 text-emerald-800' : 'bg-rose-50 border-rose-300 text-rose-800'; ?> flex items-center justify-between shadow-sm">
        <div class="flex items-center space-x-2">
            <span class="material-symbols-rounded"><?php echo ($feedback_type === 'success') ? 'check_circle' : 'error'; ?></span>
            <span class="text-sm font-semibold"><?php echo $feedback_msg; ?></span>
        </div>
        <button onclick="document.getElementById('status_alert').remove();" class="text-slate-400 hover:text-slate-600 transition">
            <span class="material-symbols-rounded text-sm">close</span>
        </button>
    </div>
<?php endif; ?>

<!-- Main Deck Header -->
<div class="mb-8 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
    <div>
        <h2 class="text-2xl font-bold tracking-tight text-slate-900">Éditeur de lot & Liste globale</h2>
        <p class="text-xs text-slate-500">Modifiez instantanément les catégories, les fiches de renseignements et les coupons cadeaux liés en masse ou un par un.</p>
    </div>
    <div class="flex space-x-2">
        <a href="commerces.php" class="px-4 py-2 bg-slate-100 hover:bg-slate-200 border border-slate-200 rounded-xl text-xs font-bold text-slate-700 transition flex items-center space-x-1">
            <span class="material-symbols-rounded text-sm">map</span>
            <span>Retour Cartographie</span>
        </a>
    </div>
</div>

<!-- PART 1: MASTER MASS ACTIONS BOARD -->
<div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm mb-8">
    <h3 class="text-sm font-bold text-slate-900 mb-2 flex items-center space-x-2">
        <span class="material-symbols-rounded text-orange-500">settings_suggest</span>
        <span>Console d'actions en masse</span>
    </h3>
    <p class="text-xs text-slate-500 mb-6">
        Cochez plusieurs établissements dans la liste ci-dessous puis configurez les valeurs ci-dessous pour appliquer une modification en groupe.
    </p>

    <div class="grid grid-cols-1 xl:grid-cols-3 gap-6">
        <!-- Mass Category -->
        <div class="p-4 bg-slate-50 border border-slate-200 rounded-xl space-y-3">
            <label class="flex items-center space-x-2 cursor-pointer">
                <input type="checkbox" id="bulk_apply_cat" class="rounded border-slate-300 text-orange-500 focus:ring-orange-500 h-4 w-4">
                <span class="text-xs font-bold text-slate-700 uppercase tracking-wider">Modifier la Catégorie</span>
            </label>
            <select id="bulk_cat_select" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-orange-500">
                <option value="Restauration">Restauration 🍔</option>
                <option value="Cave & Bar">Cave & Bar 🍷</option>
                <option value="Shopping">Shopping 🛍️</option>
                <option value="Culture">Culture 🎭</option>
                <option value="Hôtels / Logement">Hôtels 🏨</option>
                <option value="Loisirs">Loisirs 🎮</option>
            </select>
        </div>

        <!-- Mass Coupon Offer -->
        <div class="p-4 bg-slate-50 border border-slate-200 rounded-xl space-y-3">
            <label class="flex items-center space-x-2 cursor-pointer">
                <input type="checkbox" id="bulk_apply_cop" onchange="document.getElementById('bulk_remove_cop').checked = false;" class="rounded border-slate-300 text-orange-500 focus:ring-orange-500 h-4 w-4">
                <span class="text-xs font-bold text-slate-700 uppercase tracking-wider">Attribuer / Modifier Coupon</span>
            </label>
            <input type="text" id="bulk_cop_title" class="w-full px-3 py-1.5 bg-white border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-orange-500" placeholder="Ex: Tasse de café filtre offerte ☕">
            <textarea id="bulk_cop_desc" rows="1" class="w-full px-3 py-1.5 bg-white border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-orange-500" placeholder="Termes/Conditions facultatives (ex: tout achat sim...)"></textarea>
            <div class="grid grid-cols-2 gap-2">
                <input type="number" id="bulk_cop_quota" min="1" value="15" class="w-full px-3 py-1.5 bg-white border border-slate-200 rounded-lg text-xs font-mono focus:outline-none focus:border-orange-500" placeholder="Quota">
                <select id="bulk_cop_active" class="w-full px-2 py-1.5 bg-white border border-slate-200 rounded-lg text-xs focus:outline-none">
                    <option value="1">Actif 🟢</option>
                    <option value="0">Désactivé 🔴</option>
                </select>
            </div>
            
            <div class="pt-2 border-t border-slate-200/50">
                <label class="flex items-center space-x-2 cursor-pointer text-[11px] text-rose-600 font-medium">
                    <input type="checkbox" id="bulk_remove_cop" onchange="document.getElementById('bulk_apply_cop').checked = false;" class="rounded border-slate-300 text-rose-500 focus:ring-rose-500 h-3.5 w-3.5">
                    <span>Dissocier/Supprimer les coupons des cochés</span>
                </label>
            </div>
        </div>

        <!-- Apply console triggers -->
        <div class="p-4 bg-orange-50/50 border border-orange-200 rounded-xl flex flex-col justify-between">
            <div>
                <span class="text-xs font-bold text-orange-600 block tracking-wider uppercase mb-1">Valider l'action de lot</span>
                <p class="text-[11px] text-slate-500">Tous les établissements actuellement sélectionnés avec les cases de gauche recevront l'une des actions suivantes au clic.</p>
            </div>
            <div class="pt-3 border-t border-slate-200/50 mt-4 flex flex-col sm:flex-row gap-2">
                <button onclick="submitBulk('update')" class="flex-1 py-2.5 px-4 bg-orange-500 hover:bg-orange-600 text-white font-bold text-xs rounded-xl shadow-sm transition flex items-center justify-center space-x-1.1">
                    <span class="material-symbols-rounded text-sm">done_all</span>
                    <span>Mettre à jour les cochés</span>
                </button>
                <button onclick="submitBulk('delete')" class="py-2.5 px-4 bg-red-100 hover:bg-red-200 text-red-700 font-bold text-xs rounded-xl transition flex items-center justify-center space-x-1">
                    <span class="material-symbols-rounded text-sm">delete_sweep</span>
                    <span>Supprimer cochés</span>
                </button>
            </div>
        </div>
    </div>
</div>

<!-- PART 1.5: DIRECT GEOJSON IMPORT DECK -->
<div class="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm mb-8">
    <div class="flex justify-between items-center cursor-pointer" onclick="document.getElementById('compact_import_drawer').classList.toggle('hidden');">
        <h4 class="text-xs font-bold text-slate-900 uppercase tracking-widest flex items-center space-x-2">
            <span class="material-symbols-rounded text-orange-500">cloud_upload</span>
            <span>Importer / Remplacer commerces.geojson</span>
        </h4>
        <span class="text-xs text-orange-500 font-bold hover:underline flex items-center space-x-1 select-none">
            <span>Déplier / Replier</span>
            <span class="material-symbols-rounded text-sm">unfold_more</span>
        </span>
    </div>
    <div id="compact_import_drawer" class="hidden mt-4 pt-4 border-t border-slate-100">
        <p class="text-xs text-slate-500 mb-4 leading-relaxed">
            Remplacer instantanément l'intégralité du fichier <code class="bg-slate-100 px-1 py-0.5 rounded font-mono text-[10px] text-slate-700">commerces.geojson</code> et sa cartographie par un nouveau fichier GeoJSON valide de type <code class="bg-slate-100 px-1 py-0.5 rounded font-mono text-[10px] text-slate-700">FeatureCollection</code>.
        </p>
        <form action="commerces_liste.php" method="POST" enctype="multipart/form-data" class="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
            <div class="relative flex-1">
                <input type="file" name="geojson_file" accept=".geojson,.json" required 
                       class="hidden" id="direct_geojson_file_input" onchange="updateDirectFileName(this)">
                <label for="direct_geojson_file_input" 
                       class="flex items-center justify-center space-x-2 px-4 py-2.5 bg-slate-50 border border-slate-200 hover:border-orange-400 rounded-lg text-xs font-semibold text-slate-700 cursor-pointer shadow-sm transition">
                    <span class="material-symbols-rounded text-sm text-slate-400">upload_file</span>
                    <span id="direct_file_label_txt">Choisir un fichier GeoJSON...</span>
                </label>
            </div>
            <button type="submit" class="px-5 py-2.5 bg-orange-500 hover:bg-orange-600 text-white text-xs font-bold rounded-lg shadow-sm transition flex items-center justify-center space-x-1.5">
                <span class="material-symbols-rounded text-sm">cloud_upload</span>
                <span>Lancer l'import direct</span>
            </button>
        </form>
    </div>
</div>
<script>
function updateDirectFileName(input) {
    const txtEl = document.getElementById('direct_file_label_txt');
    if (input.files && input.files[0]) {
        txtEl.textContent = input.files[0].name;
    } else {
        txtEl.textContent = "Choisir un fichier GeoJSON...";
    }
}
</script>

<!-- PART 1.6: SMART AUTO-CATEGORIZATION ASSISTANT -->
<div class="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm mb-8">
    <div class="flex justify-between items-center cursor-pointer" onclick="document.getElementById('smart_autocat_drawer').classList.toggle('hidden');">
        <h4 class="text-xs font-bold text-slate-900 uppercase tracking-widest flex items-center space-x-2">
            <span class="material-symbols-rounded text-orange-500">magic_button</span>
            <span>Assistant de Catégorisation Intelligente (Recommandé)</span>
        </h4>
        <span class="text-xs text-orange-500 font-bold hover:underline flex items-center space-x-1 select-none">
            <span>Déplier / Replier</span>
            <span class="material-symbols-rounded text-sm">unfold_more</span>
        </span>
    </div>
    <div id="smart_autocat_drawer" class="mt-4 pt-4 border-t border-slate-100">
        <p class="text-xs text-slate-500 mb-4 leading-relaxed">
            Cet outil analyse les attributs natifs OpenStreetMap de votre GeoJSON (les clés comme <code class="bg-slate-100 px-1 py-0.5 rounded font-mono text-[10px] text-slate-700">shop</code>, <code class="bg-slate-100 px-1 py-0.5 rounded font-mono text-[10px] text-slate-700">amenity</code>, <code class="bg-slate-100 px-1 py-0.5 rounded font-mono text-[10px] text-slate-700">tourism</code> ou <code class="bg-slate-100 px-1 py-0.5 rounded font-mono text-[10px] text-slate-700">cuisine</code>) et configure automatiquement la catégorie principale de l'application (🍔 Restauration, 🍷 Cave & Bar, 🛍️ Shopping, etc.) ainsi qu'une description de sous-catégorie appropriée.
        </p>
        <form action="commerces_liste.php" method="POST" class="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4 bg-slate-50 p-4 border border-slate-200 rounded-xl">
            <input type="hidden" name="action" value="auto_categorize">
            <div class="flex items-center space-x-3">
                <input type="checkbox" name="overwrite" value="1" id="overwrite_autocat" class="rounded border-slate-300 text-orange-600 focus:ring-orange-500 h-4 w-4">
                <label for="overwrite_autocat" class="text-xs font-semibold text-slate-700 cursor-pointer select-none">
                    Écraser les catégories existantes (Forcer une ré-évaluation complète)
                </label>
            </div>
            <button type="submit" class="px-5 py-2.5 bg-gradient-to-r from-orange-500 to-amber-500 hover:from-orange-600 hover:to-amber-600 text-white text-xs font-bold rounded-lg shadow-sm transition flex items-center justify-center space-x-2">
                <span class="material-symbols-rounded text-sm">auto_awesome</span>
                <span>Lancer la Catégorisation Automatique</span>
            </button>
        </form>
    </div>
</div>

<!-- PART 2: THE BUSINESS LIST TABLE -->
<div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
    
    <!-- Local search and filter tools -->
    <div class="p-5 border-b border-slate-200 bg-slate-50/50 flex flex-col sm:flex-row gap-4 items-center justify-between">
        <div class="flex items-center space-x-3 w-full sm:w-auto">
            <span class="text-sm font-bold text-slate-800">Partenaires Écusson (<?php echo count($commerces); ?>)</span>
        </div>
        <div class="flex flex-col sm:flex-row gap-2 w-full sm:w-auto">
            <input type="text" id="local_search_input" onkeyup="filterCommercesTable()" placeholder="Rechercher par nom..." 
                   class="px-3 py-2 border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-1 focus:ring-orange-500 bg-white">
            <select id="local_category_filter" onchange="filterCommercesTable()" class="px-3 py-2 border border-slate-200 rounded-xl text-xs focus:outline-none bg-white">
                <option value="all">Séléction Haute Catégorie -- (Tout)</option>
                <option value="Restauration">Restauration</option>
                <option value="Cave & Bar">Cave & Bar</option>
                <option value="Shopping">Shopping</option>
                <option value="Culture">Culture</option>
                <option value="Hôtels / Logement">Hôtels</option>
                <option value="Loisirs">Loisirs</option>
            </select>
        </div>
    </div>

    <!-- Main List Presentation -->
    <?php if (!empty($commerces)): ?>
        <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse" id="commerces_list_table">
                <thead>
                    <tr class="border-b border-slate-200 text-[10px] font-bold text-slate-400 uppercase tracking-wider bg-slate-50">
                        <th class="py-3 px-4 w-12 text-center">
                            <input type="checkbox" id="toggle_master_checkbox" onclick="toggleAllCheckboxes(this)" class="rounded border-slate-300 text-orange-500 focus:ring-orange-500 h-4 w-4 cursor-pointer">
                        </th>
                        <th class="py-3 px-4">Établissement & Photo</th>
                        <th class="py-3 px-4">Catégorie</th>
                        <th class="py-3 px-4">Avantage / Coupon Lié</th>
                        <th class="py-3 px-4 text-right">Actions</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100 text-sm">
                    <?php foreach ($commerces as $feature):
                        $props = $feature['properties'];
                        $id = $props['id'] ?? '';
                        $coords = get_point_coordinates($feature['geometry'] ?? []);
                        $img_name = $props['github_image'] ?? '';
                        $img_url = !empty($img_name) 
                            ? "https://raw.githubusercontent.com/akkim-djenadi/LPC-final-/main/images_commerces/" . urlencode($img_name)
                            : "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=150&q=80";
                        
                        $has_coupon = isset($merchant_coupons[$id]);
                        $my_coupon = $has_coupon ? $merchant_coupons[$id] : null;
                    ?>
                        <!-- Row Display -->
                        <tr class="commerce-row hover:bg-slate-50/50" data-name="<?php echo htmlspecialchars(strtolower($props['name'] ?? '')); ?>" data-category="<?php echo htmlspecialchars($props['category'] ?? ''); ?>">
                            <td class="py-4 px-4 text-center">
                                <input type="checkbox" value="<?php echo htmlspecialchars($id); ?>" class="partner-checkbox rounded border-slate-300 text-orange-500 focus:ring-orange-500 h-4 w-4 cursor-pointer" onclick="toggleSelectionHighlighter(this)">
                            </td>
                            <td class="py-4 px-4">
                                <div class="flex items-center space-x-3">
                                    <div class="h-10 w-10 rounded-lg bg-slate-100 border border-slate-200 overflow-hidden flex-shrink-0">
                                        <img src="<?php echo htmlspecialchars($img_url); ?>" alt="Ets" class="h-full w-full object-cover">
                                    </div>
                                    <div>
                                        <p class="font-bold text-slate-800 text-xs inline-flex items-center">
                                            <span><?php echo htmlspecialchars($props['name'] ?? 'Inconnu'); ?></span>
                                            <span class="text-[10px] font-normal text-slate-400 code-font ml-1.5">(ID: <?php echo htmlspecialchars($id); ?>)</span>
                                        </p>
                                        <p class="text-[10px] text-slate-400 max-w-sm truncate"><?php echo htmlspecialchars($props['description'] ?? 'Aucune description rédigée.'); ?></p>
                                    </div>
                                </div>
                            </td>
                            <td class="py-4 px-4">
                                <span class="inline-flex px-2 py-0.5 rounded text-[10px] font-bold <?php 
                                    switch($props['category'] ?? '') {
                                        case 'Restauration': echo 'bg-indigo-50 text-indigo-700'; break;
                                        case 'Cave & Bar': echo 'bg-emerald-50 text-emerald-700'; break;
                                        case 'Shopping': echo 'bg-amber-50 text-amber-700'; break;
                                        default: echo 'bg-slate-100 text-slate-700'; break;
                                    }
                                ?>"><?php echo htmlspecialchars($props['category'] ?? 'Autre'); ?></span>
                                <p class="text-[10px] text-slate-400 font-mono mt-0.5"><?php echo htmlspecialchars($props['phone'] ?? 'Pas de numéro'); ?></p>
                            </td>
                            <td class="py-4 px-4">
                                <?php if ($has_coupon): ?>
                                    <div class="space-y-0.5">
                                        <p class="text-xs font-bold text-orange-600 flex items-center">
                                            <span class="material-symbols-rounded text-xs mr-0.5">confirmation_number</span>
                                            <span><?php echo htmlspecialchars($my_coupon['title'] ?? ''); ?></span>
                                        </p>
                                        <p class="text-[10px] text-slate-500 font-medium">Tickets restants : <span class="font-mono bg-slate-100 px-1 py-0.2 rounded font-bold text-slate-700"><?php echo intval($my_coupon['quota'] ?? 0); ?></span></p>
                                    </div>
                                <?php else: ?>
                                    <span class="text-slate-400 text-xs italic flex items-center">
                                        <span class="material-symbols-rounded text-xs mr-0.5">warning</span>
                                        <span>Aucun coupon avantage actif</span>
                                    </span>
                                <?php endif; ?>
                            </td>
                            <td class="py-4 px-4 text-right">
                                <button onclick="toggleExpandRow('<?php echo htmlspecialchars($id); ?>')" class="py-1 px-3 text-xs font-bold rounded-lg border border-slate-200 bg-white hover:bg-slate-100 text-slate-700 shadow-sm transition flex items-center inline-flex space-x-1">
                                    <span class="material-symbols-rounded text-sm">edit_note</span>
                                    <span>Modifier la fiche</span>
                                </button>
                            </td>
                        </tr>

                        <!-- Expandable Sub-Form Accordion Row -->
                        <tr id="expand_<?php echo htmlspecialchars($id); ?>" class="hidden bg-slate-50/70 border-t border-b border-orange-100">
                            <td colspan="5" class="p-6">
                                <form action="commerces_liste.php" method="POST" class="space-y-5">
                                    <input type="hidden" name="action" value="save_single">
                                    <input type="hidden" name="merchant_id" value="<?php echo htmlspecialchars($id); ?>">

                                    <div class="grid grid-cols-1 xl:grid-cols-2 gap-8">
                                        <!-- Column Left: Business Details -->
                                        <div class="space-y-4">
                                            <h4 class="text-xs font-bold text-slate-900 uppercase tracking-widest flex items-center space-x-2 border-b border-slate-200 pb-2">
                                                <span class="material-symbols-rounded text-sm text-orange-500">storefront</span>
                                                <span>Fiche d'Établissement & Coordonnées</span>
                                            </h4>

                                            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                                <div>
                                                    <label class="block text-xs font-bold text-slate-600 uppercase mb-1">Nom d'enseigne *</label>
                                                    <input type="text" name="name" required class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-orange-500 font-semibold" value="<?php echo htmlspecialchars($props['name'] ?? ''); ?>">
                                                </div>
                                                <div>
                                                    <label class="block text-xs font-bold text-slate-600 uppercase mb-1">Catégorie principal</label>
                                                    <select name="category" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-orange-500">
                                                        <option value="Restauration" <?php echo (($props['category'] ?? '') == 'Restauration') ? 'selected' : ''; ?>>Restauration 🍔</option>
                                                        <option value="Cave & Bar" <?php echo (($props['category'] ?? '') == 'Cave & Bar') ? 'selected' : ''; ?>>Cave & Bar 🍷</option>
                                                        <option value="Shopping" <?php echo (($props['category'] ?? '') == 'Shopping') ? 'selected' : ''; ?>>Shopping 🛍️</option>
                                                        <option value="Culture" <?php echo (($props['category'] ?? '') == 'Culture') ? 'selected' : ''; ?>>Culture 🎭</option>
                                                        <option value="Hôtels / Logement" <?php echo (($props['category'] ?? '') == 'Hôtels / Logement') ? 'selected' : ''; ?>>Hôtels 🏨</option>
                                                        <option value="Loisirs" <?php echo (($props['category'] ?? '') == 'Loisirs') ? 'selected' : ''; ?>>Loisirs 🎮</option>
                                                    </select>
                                                </div>
                                            </div>

                                            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                                <div>
                                                    <label class="block text-xs font-bold text-slate-600 uppercase mb-1">Sous-catégorie</label>
                                                    <input type="text" name="subcategory" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-orange-500" value="<?php echo htmlspecialchars($props['subcategory'] ?? ''); ?>" placeholder="Ex: Crêperie bretonne">
                                                </div>
                                                <div>
                                                    <label class="block text-xs font-bold text-slate-600 uppercase mb-1">Téléphone</label>
                                                    <input type="text" name="phone" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-orange-500" value="<?php echo htmlspecialchars($props['phone'] ?? ''); ?>">
                                                </div>
                                            </div>

                                            <div class="grid grid-cols-2 gap-4">
                                                <div>
                                                    <label class="block text-xs font-bold text-slate-600 uppercase mb-1">Latitude (Y Coords)</label>
                                                    <input type="text" name="lat" required class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs font-mono focus:outline-none focus:border-orange-500" value="<?php echo htmlspecialchars($coords[1]); ?>">
                                                </div>
                                                <div>
                                                    <label class="block text-xs font-bold text-slate-600 uppercase mb-1">Longitude (X Coords)</label>
                                                    <input type="text" name="lng" required class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs font-mono focus:outline-none focus:border-orange-500" value="<?php echo htmlspecialchars($coords[0]); ?>">
                                                </div>
                                            </div>

                                            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                                <div>
                                                    <label class="block text-xs font-bold text-slate-600 uppercase mb-1">Nom Photo sur GitHub</label>
                                                    <input type="text" name="github_image" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-orange-500" value="<?php echo htmlspecialchars($props['github_image'] ?? ''); ?>" placeholder="Ex: saint_jean.jpg">
                                                </div>
                                                <div>
                                                    <label class="block text-xs font-bold text-slate-400 uppercase mb-1">CDN Image brut (prévisualisation)</label>
                                                    <span class="text-[10px] tracking-tight truncate block p-2 bg-slate-100 rounded border border-slate-200 mt-1 max-w-xs text-slate-500">
                                                        <?php echo !empty($img_name) ? "raw.githubusercontent.com/.../".htmlspecialchars($img_name) : "Aucun fichier photo GitHub rattaché"; ?>
                                                    </span>
                                                </div>
                                            </div>

                                            <div>
                                                <label class="block text-xs font-bold text-slate-600 uppercase mb-1">Description</label>
                                                <textarea name="description" rows="3" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-orange-500" placeholder="Entrez une brève description du commerce..."><?php echo htmlspecialchars($props['description'] ?? ''); ?></textarea>
                                            </div>
                                        </div>

                                        <!-- Column Right: Custom reward coupon inline mutation -->
                                        <div class="space-y-4">
                                            <h4 class="text-xs font-bold text-slate-900 uppercase tracking-widest flex items-center space-x-2 border-b border-slate-200 pb-2">
                                                <span class="material-symbols-rounded text-sm text-orange-500">confirmation_number</span>
                                                <span>Coupon en jeu (Bonus Fidélité client)</span>
                                            </h4>

                                            <div>
                                                <label class="block text-xs font-bold text-slate-600 uppercase mb-1">Titre de l'Offre / Avantage</label>
                                                <input type="text" name="coupon_title" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-orange-500" placeholder="Ex: Tasse de thé glacé offerte avec crumble !" value="<?php echo htmlspecialchars($my_coupon['title'] ?? ''); ?>">
                                                <p class="text-[10px] text-slate-400 mt-1">Laissez ce titre vide pour supprimer/ne pas rattacher de coupon cadeau à cet établissement.</p>
                                            </div>

                                            <div>
                                                <label class="block text-xs font-bold text-slate-600 uppercase mb-1">Conditions de retrait (termes & conditions)</label>
                                                <textarea name="coupon_description" rows="3" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-orange-500" placeholder="Ex: Offert pour l'achat simultané d'une formule midi."><?php echo htmlspecialchars($my_coupon['description'] ?? ''); ?></textarea>
                                            </div>

                                            <div class="grid grid-cols-2 gap-4">
                                                <div>
                                                    <label class="block text-xs font-bold text-slate-600 uppercase mb-1">Nombre (Tickets / Quota)</label>
                                                    <input type="number" name="coupon_quota" min="1" max="1000" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs font-mono focus:outline-none focus:border-orange-500" value="<?php echo htmlspecialchars($my_coupon['quota'] ?? 15); ?>">
                                                </div>
                                                <div>
                                                    <label class="block text-xs font-bold text-slate-600 uppercase mb-1">Statut Initial</label>
                                                    <select name="coupon_active" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs focus:outline-none">
                                                        <option value="1" <?php echo ($my_coupon && $my_coupon['active'] === true) ? 'selected' : ''; ?>>Disponible immédiatement 🟢</option>
                                                        <option value="0" <?php echo ($my_coupon && $my_coupon['active'] === false) ? 'selected' : ''; ?>>Désactivé temporairement 🔴</option>
                                                    </select>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Bottom form actions row -->
                                    <div class="pt-4 border-t border-slate-200 flex justify-end space-x-2">
                                        <button type="button" onclick="toggleExpandRow('<?php echo htmlspecialchars($id); ?>')" class="py-2 px-4 rounded-xl border border-slate-200 bg-white hover:bg-slate-100 text-slate-700 text-xs font-semibold shadow-sm transition">
                                            Annuler & Fermer
                                        </button>
                                        <button type="submit" class="py-2 px-5 rounded-xl bg-orange-500 hover:bg-orange-600 text-white text-xs font-bold shadow-sm transition flex items-center space-x-1">
                                            <span class="material-symbols-rounded text-sm">save</span>
                                            <span>Sauvegarder la Fiche</span>
                                        </button>
                                    </div>
                                </form>
                            </td>
                        </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    <?php else: ?>
        <div class="p-12 text-center text-slate-500 italic block">
            <span class="material-symbols-rounded text-4xl block text-slate-300 mb-2">storefront</span>
            Il n'y a aucun commerce enregistré dans votre fichier commerces.geojson. <br>
            Allez sur l'onglet <a href="commerces.php" class="text-orange-500 underline font-bold">Commerces & Carto</a> pour ajouter du contenu.
        </div>
    <?php endif; ?>
</div>

<!-- REAL HIDDEN BULK FORM -->
<form id="real_bulk_form" action="commerces_liste.php" method="POST" class="hidden">
    <input type="hidden" name="action" value="bulk_save">
    <input type="hidden" name="bulk_action" id="bulk_action_input" value="">
    <input type="hidden" name="bulk_category" id="bulk_category_input" value="">
    <input type="hidden" name="apply_category" id="bulk_apply_category_input" value="">
    <input type="hidden" name="bulk_coupon_title" id="bulk_coupon_title_input" value="">
    <input type="hidden" name="bulk_coupon_description" id="bulk_coupon_description_input" value="">
    <input type="hidden" name="bulk_coupon_quota" id="bulk_coupon_quota_input" value="">
    <input type="hidden" name="bulk_coupon_active" id="bulk_coupon_active_input" value="">
    <input type="hidden" name="bulk_remove_coupons" id="bulk_remove_coupons_input" value="">
    <input type="hidden" name="apply_coupon" id="bulk_apply_coupon_input" value="">
</form>

<!-- LIST & BATCH INTERACTIVE JAVASCRIPT LOGIC -->
<script>
// Expand/Collapse individual editable rows
function toggleExpandRow(id) {
    const expandRow = document.getElementById('expand_' + id);
    if (expandRow) {
        expandRow.classList.toggle('hidden');
    }
}

// Check/Uncheck every single item row checkbox
function toggleAllCheckboxes(masterCheckbox) {
    const checkboxes = document.querySelectorAll('.partner-checkbox');
    checkboxes.forEach(box => {
        box.checked = masterCheckbox.checked;
        toggleSelectionHighlighter(box);
    });
}

// Highlight modified row backgrounds
function toggleSelectionHighlighter(checkbox) {
    const row = checkbox.closest('tr');
    if (checkbox.checked) {
        row.classList.add('bg-orange-50/20');
        row.classList.add('border-l-4');
        row.classList.add('border-l-orange-500');
    } else {
        row.classList.remove('bg-orange-50/20');
        row.classList.remove('border-l-4');
        row.classList.remove('border-l-orange-500');
    }
}

// Live client-side filtering by name / categories
function filterCommercesTable() {
    const query = document.getElementById('local_search_input').value.toLowerCase().trim();
    const catFilter = document.getElementById('local_category_filter').value.toLowerCase().trim();
    const rows = document.querySelectorAll('.commerce-row');
    
    rows.forEach(row => {
        const name = (row.getAttribute('data-name') || '').toLowerCase().trim();
        const category = (row.getAttribute('data-category') || '').toLowerCase().trim();
        
        const matchSearch = (query === '' || name.includes(query));
        const matchCategory = (catFilter === 'all' || category === catFilter || category.includes(catFilter));
        
        // Find expanded row as well
        const subRowId = row.nextElementSibling ? row.nextElementSibling.id : '';
        const subRow = (subRowId && subRowId.startsWith('expand_')) ? row.nextElementSibling : null;
        
        if (matchSearch && matchCategory) {
            row.classList.remove('hidden');
        } else {
            row.classList.add('hidden');
            if (subRow) {
                subRow.classList.add('hidden'); // fold back
            }
        }
    });
}

// Bulk submit launcher
function submitBulk(bulkAction) {
    const checkedBoxes = document.querySelectorAll('.partner-checkbox:checked');
    if (checkedBoxes.length === 0) {
        alert("⚠️ Veuillez cocher au moins un établissement dans la liste ci-dessous.");
        return;
    }
    
    if (bulkAction === 'delete') {
        if (!confirm(`🗑️ Êtes-vous sûr de vouloir supprimer définitivement les ${checkedBoxes.length} établissements sélectionnés d'Écusson ?`)) {
            return;
        }
    }
    
    const form = document.getElementById('real_bulk_form');
    // Sanity cleanup of previous dynamic nodes
    const existing = form.querySelectorAll('.dynamic-selected');
    existing.forEach(el => el.remove());
    
    // Inject checked partner IDs
    checkedBoxes.forEach(box => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'selected_ids[]';
        input.value = box.value;
        input.className = 'dynamic-selected';
        form.appendChild(input);
    });
    
    document.getElementById('bulk_action_input').value = bulkAction;
    
    // Inject bulk details
    if (bulkAction === 'update') {
        const catCheck = document.getElementById('bulk_apply_cat');
        document.getElementById('bulk_apply_category_input').value = catCheck.checked ? "1" : "0";
        document.getElementById('bulk_category_input').value = document.getElementById('bulk_cat_select').value;
        
        const copCheck = document.getElementById('bulk_apply_cop');
        document.getElementById('bulk_apply_coupon_input').value = copCheck.checked ? "1" : "0";
        document.getElementById('bulk_coupon_title_input').value = document.getElementById('bulk_cop_title').value;
        document.getElementById('bulk_coupon_description_input').value = document.getElementById('bulk_cop_desc').value;
        document.getElementById('bulk_coupon_quota_input').value = document.getElementById('bulk_cop_quota').value;
        document.getElementById('bulk_coupon_active_input').value = document.getElementById('bulk_cop_active').value;
        
        const rCopCheck = document.getElementById('bulk_remove_cop');
        document.getElementById('bulk_remove_coupons_input').value = rCopCheck.checked ? "1" : "0";
    }
    
    form.submit();
}
</script>

<?php include 'footer.php'; ?>
