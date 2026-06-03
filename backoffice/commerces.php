<?php
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
$commerces = [];
$feedback_msg = "";
$feedback_type = "success"; // success or error

if (!function_exists('ultra_clean')) {
    function ultra_clean($str) {
        $str = mb_strtolower($str, 'UTF-8');
        $str = str_replace('&', 'et', $str);
        $transliterator = array(
            'à'=>'a', 'á'=>'a', 'â'=>'a', 'ã'=>'a', 'ä'=>'a', 'å'=>'a', 'æ'=>'ae', 'ç'=>'c',
            'è'=>'e', 'é'=>'e', 'ê'=>'e', 'ë'=>'e', 'ì'=>'i', 'í'=>'i', 'î'=>'i', 'ï'=>'i',
            'ð'=>'d', 'ñ'=>'n', 'ò'=>'o', 'ó'=>'o', 'ô'=>'o', 'õ'=>'o', 'ö'=>'o', 'ø'=>'o',
            'ù'=>'u', 'ú'=>'u', 'û'=>'u', 'ü'=>'u', 'ý'=>'y', 'ÿ'=>'y', 'œ'=>'oe', 'ß'=>'ss',
            '’'=>'', '\''=>''
        );
        $str = strtr($str, $transliterator);
        $str = preg_replace('/[^a-z0-9]/', '', $str);
        return $str;
    }
}

if (!function_exists('remove_french_prefixes_str')) {
    function remove_french_prefixes_str($str) {
        return preg_replace('/^(le|la|les|un|une|du|de|des|au|aux|l|d)/', '', $str);
    }
}

if (!function_exists('find_closest_image')) {
    function find_closest_image($name) {
        static $images = null;
        if ($images === null) {
            $file = __DIR__ . '/data/images_list.txt';
            if (file_exists($file) && is_readable($file)) {
                $content = file_get_contents($file);
                $images = preg_split('/\r\n|\r|\n/', $content);
                $images = array_filter(array_map('trim', $images));
            } else {
                $images = array();
            }
        }
        
        if (empty($images)) {
            return '';
        }
        
        $clean_name = ultra_clean($name);
        $clean_name_noprefix = remove_french_prefixes_str($clean_name);
        
        $fuzzy_match = '';
        
        foreach ($images as $img) {
            $img_no_ext = pathinfo($img, PATHINFO_FILENAME);
            $clean_img = ultra_clean($img_no_ext);
            $clean_img_noprefix = remove_french_prefixes_str($clean_img);
            
            // Match 1: Extract match
            if ($clean_name === $clean_img) {
                return $img;
            }
            
            // Match 2: Prefix-removed match
            if (!empty($clean_name_noprefix) && !empty($clean_img_noprefix) && $clean_name_noprefix === $clean_img_noprefix) {
                return $img;
            }
            
            // Match 3: Check if either is a substring of another
            if (empty($fuzzy_match)) {
                if (!empty($clean_name) && !empty($clean_img)) {
                    if (strpos($clean_name, $clean_img) !== false || strpos($clean_img, $clean_name) !== false) {
                        $fuzzy_match = $img;
                    }
                }
            }
        }
        
        return $fuzzy_match;
    }
}

// Read GeoJSON safely
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

// HANDLE FORM SUBMISSION: ADD / EDIT
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (isset($_POST['action']) && ($_POST['action'] === 'add' || $_POST['action'] === 'edit')) {
        $name = trim($_POST['name'] ?? '');
        $category = trim($_POST['category'] ?? 'Restauration');
        $subcategory = trim($_POST['subcategory'] ?? '');
        $description = trim($_POST['description'] ?? '');
        $github_image = trim($_POST['github_image'] ?? '');
        $phone = trim($_POST['phone'] ?? '');
        $lat = floatval($_POST['lat'] ?? 43.6085);
        $lng = floatval($_POST['lng'] ?? 3.8794);
        
        if (empty($name)) {
            $feedback_msg = "❌ Le nom du commerce ne peut pas être vide.";
            $feedback_type = "error";
        } else {
            if ($_POST['action'] === 'add') {
                $id = 'mer_' . strtolower(preg_replace('/[^a-zA-Z0-9]/', '', $name)) . '_' . rand(100, 999);
                
                $new_feature = [
                    "type" => "Feature",
                    "properties" => [
                        "id" => $id,
                        "name" => $name,
                        "category" => $category,
                        "subcategory" => $subcategory,
                        "description" => $description,
                        "github_image" => $github_image,
                        "phone" => $phone
                    ],
                    "geometry" => [
                        "type" => "Point",
                        "coordinates" => [$lng, $lat]
                    ]
                ];
                
                $commerces[] = $new_feature;
                $feedback_msg = "✅ Succès ! L'établissement '$name' a été ajouté au fichier GeoJSON !";
            } else {
                // Edit
                $id = $_POST['id'] ?? '';
                $found = false;
                foreach ($commerces as $key => $feature) {
                    if (isset($feature['properties']['id']) && $feature['properties']['id'] === $id) {
                        $commerces[$key]['properties']['name'] = $name;
                        $commerces[$key]['properties']['category'] = $category;
                        $commerces[$key]['properties']['subcategory'] = $subcategory;
                        $commerces[$key]['properties']['description'] = $description;
                        $commerces[$key]['properties']['github_image'] = $github_image;
                        $commerces[$key]['properties']['phone'] = $phone;
                        $commerces[$key]['geometry']['coordinates'] = [$lng, $lat];
                        $found = true;
                        break;
                    }
                }
                if ($found) {
                    $feedback_msg = "✅ Succès ! L'établissement '$name' a été mis à jour avec succès.";
                } else {
                    $feedback_msg = "❌ Échec de la mise à jour : établissement non trouvé.";
                    $feedback_type = "error";
                }
            }
            
            // Save back to geojson
            $new_geojson = [
                "type" => "FeatureCollection",
                "features" => array_values($commerces)
            ];
            file_put_contents($commerces_file, json_encode($new_geojson, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
        }
    }
    
    // HANDLE DELETE
    if (isset($_POST['action']) && $_POST['action'] === 'delete') {
        $id = $_POST['id'] ?? '';
        $initial_count = count($commerces);
        $commerces = array_filter($commerces, function($feature) use ($id) {
            return ($feature['properties']['id'] ?? '') !== $id;
        });
        
        if (count($commerces) < $initial_count) {
            $new_geojson = [
                "type" => "FeatureCollection",
                "features" => array_values($commerces)
            ];
            file_put_contents($commerces_file, json_encode($new_geojson, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            $feedback_msg = "🗑️ L'établissement a été supprimé avec succès.";
        } else {
            $feedback_msg = "❌ L'élement n'a pas pu être supprimé.";
            $feedback_type = "error";
        }
    }
}

// Prefill form for editing
$edit_mode = false;
$edit_item = [
    'id' => '', 'name' => '', 'category' => 'Restauration', 'subcategory' => '',
    'description' => '', 'github_image' => '', 'phone' => '', 'lat' => 43.6085, 'lng' => 3.8794
];
if (isset($_GET['edit_id'])) {
    $edit_id = $_GET['edit_id'];
    foreach ($commerces as $f) {
        if (($f['properties']['id'] ?? '') === $edit_id) {
            $edit_mode = true;
            $edit_item = [
                'id' => $f['properties']['id'],
                'name' => $f['properties']['name'] ?? '',
                'category' => $f['properties']['category'] ?? 'Restauration',
                'subcategory' => $f['properties']['subcategory'] ?? '',
                'description' => $f['properties']['description'] ?? '',
                'github_image' => $f['properties']['github_image'] ?? '',
                'phone' => $f['properties']['phone'] ?? '',
                'lat' => $f['geometry']['coordinates'][1] ?? 43.6085,
                'lng' => $f['geometry']['coordinates'][0] ?? 3.8794
            ];
            break;
        }
    }
}

include 'header.php';
?>



<!-- Feedback Messages -->
<?php if (!empty($feedback_msg)): ?>
    <div class="auto-dismiss mb-6 p-4 rounded-xl border <?php echo ($feedback_type === 'success') ? 'bg-emerald-50 border-emerald-300 text-emerald-800' : 'bg-rose-50 border-rose-300 text-rose-800'; ?> flex items-center space-x-2">
        <span class="material-symbols-rounded"><?php echo ($feedback_type === 'success') ? 'check_circle' : 'error'; ?></span>
        <span class="text-sm font-semibold"><?php echo $feedback_msg; ?></span>
    </div>
<?php endif; ?>

<div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
    <!-- Form to Add/Edit Establishments -->
    <div class="lg:col-span-1">
        <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm sticky top-6">
            <h3 class="text-lg font-bold text-slate-900 mb-4 flex items-center space-x-2">
                <span class="material-symbols-rounded text-orange-500"><?php echo $edit_mode ? 'edit' : 'add_location'; ?></span>
                <span><?php echo $edit_mode ? "Modifier l'établissement" : "Ajouter un établissement"; ?></span>
            </h3>
            
            <form action="commerces.php" method="POST" class="space-y-4">
                <input type="hidden" name="action" value="<?php echo $edit_mode ? 'edit' : 'add'; ?>">
                <?php if ($edit_mode): ?>
                    <input type="hidden" name="id" value="<?php echo htmlspecialchars($edit_item['id']); ?>">
                <?php endif; ?>
                
                <div>
                    <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Nom du commerce *</label>
                    <input type="text" name="name" required class="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:border-orange-500 focus:outline-none" placeholder="Ex: Vignoble de la Babote" value="<?php echo htmlspecialchars($edit_item['name']); ?>">
                </div>

                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Catégorie principal</label>
                        <select name="category" class="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:border-orange-500 focus:outline-none">
                            <option value="Restauration" <?php echo ($edit_item['category'] == 'Restauration') ? 'selected' : ''; ?>>Restauration</option>
                            <option value="Cave & Bar" <?php echo ($edit_item['category'] == 'Cave & Bar') ? 'selected' : ''; ?>>Cave & Bar</option>
                            <option value="Shopping" <?php echo ($edit_item['category'] == 'Shopping') ? 'selected' : ''; ?>>Shopping</option>
                            <option value="Culture" <?php echo ($edit_item['category'] == 'Culture') ? 'selected' : ''; ?>>Culture</option>
                            <option value="Hôtels / Logement" <?php echo ($edit_item['category'] == 'Hôtels / Logement') ? 'selected' : ''; ?>>Hôtels</option>
                            <option value="Loisirs" <?php echo ($edit_item['category'] == 'Loisirs') ? 'selected' : ''; ?>>Loisirs</option>
                        </select>
                    </div>
                    <div>
                        <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Sous-catégorie</label>
                        <input type="text" name="subcategory" class="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:border-orange-500 focus:outline-none" placeholder="Ex: Restaurant / Crêperie" value="<?php echo htmlspecialchars($edit_item['subcategory']); ?>">
                    </div>
                </div>

                <div>
                    <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Description courte</label>
                    <textarea name="description" rows="3" class="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:border-orange-500 focus:outline-none" placeholder="Description des services à afficher sur l'app de Montpellier..."><?php echo htmlspecialchars($edit_item['description']); ?></textarea>
                </div>

                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Latitude (GPS) *</label>
                        <input type="text" name="lat" required id="input_latitude" class="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-xs font-mono focus:border-orange-500 focus:outline-none" placeholder="Ex: 43.6085" value="<?php echo htmlspecialchars($edit_item['lat']); ?>">
                    </div>
                    <div>
                        <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Longitude (GPS) *</label>
                        <input type="text" name="lng" required id="input_longitude" class="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-xs font-mono focus:border-orange-500 focus:outline-none" placeholder="Ex: 3.8794" value="<?php echo htmlspecialchars($edit_item['lng']); ?>">
                    </div>
                </div>

                <div>
                    <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Nom Photo sur GitHub</label>
                    <div class="flex space-x-2">
                        <span class="inline-flex items-center px-2 bg-slate-100 border border-r-0 border-slate-200 rounded-l-xl text-slate-400 text-xs">/images_commerces/</span>
                        <input type="text" name="github_image" class="w-full px-4 py-2.5 rounded-r-xl border border-slate-200 text-sm focus:border-orange-500 focus:outline-none" placeholder="Ex: bouchon.jpg" value="<?php echo htmlspecialchars($edit_item['github_image']); ?>">
                    </div>
                    <p class="text-[10px] text-slate-500 mt-1 italic">Lié directement au dépôt de photo de votre dépôt GitHub.</p>
                </div>

                <div>
                    <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">N° Téléphone</label>
                    <input type="text" name="phone" class="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:border-orange-500 focus:outline-none" placeholder="Ex: 04 67..." value="<?php echo htmlspecialchars($edit_item['phone']); ?>">
                </div>

                <div class="flex space-x-3 pt-2">
                    <button type="submit" class="flex-1 py-3 px-4 rounded-xl bg-orange-500 hover:bg-orange-600 font-bold text-white text-sm transition duration-150">
                        <?php echo $edit_mode ? 'Sauvegarder les modifications' : "Créer l'établissement"; ?>
                    </button>
                    <?php if ($edit_mode): ?>
                        <a href="commerces.php" class="py-3 px-4 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-sm font-semibold transition duration-150">
                            Annuler
                        </a>
                    <?php endif; ?>
                </div>
            </form>
        </div>
    </div>

    <!-- Active Establishments Table -->
    <div class="lg:col-span-2 space-y-6">


        <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
            <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
                <div>
                    <h3 class="text-lg font-bold text-slate-900">Liste de vos Établissements</h3>
                    <p class="text-xs text-slate-500">Filtrage et prévisualisation directe à partir de votre GitHub</p>
                </div>
                <!-- Categories Quick Indicator Legend -->
                <div class="flex flex-wrap gap-2">
                    <span class="px-2 py-0.5 rounded-full text-[10px]/normal font-semibold bg-indigo-50 text-indigo-700 border border-indigo-200">Restauration</span>
                    <span class="px-2 py-0.5 rounded-full text-[10px]/normal font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">Cave & Bar</span>
                    <span class="px-2 py-0.5 rounded-full text-[10px]/normal font-semibold bg-amber-50 text-amber-700 border border-amber-200">Shopping</span>
                    <span class="px-2 py-0.5 rounded-full text-[10px]/normal font-semibold bg-pink-50 text-pink-700 border border-pink-200">Culture</span>
                </div>
            </div>

            <?php if (!empty($commerces)): ?>
                <div class="overflow-x-auto">
                    <table class="w-full text-left border-collapse">
                        <thead>
                            <tr class="border-b border-slate-200 text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                                <th class="py-3 px-4">Établissement & Photo</th>
                                <th class="py-3 px-4">Catégories & Info</th>
                                <th class="py-3 px-4">Localisation (Coords)</th>
                                <th class="py-3 px-4 text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-slate-100 text-sm">
                            <?php foreach ($commerces as $feature): 
                                $props = $feature['properties'];
                                $coords = get_point_coordinates($feature['geometry'] ?? []);
                                $img_name = $props['github_image'] ?? '';
                                if (empty($img_name)) {
                                    $existing_img = $props['image_url'] ?? '';
                                    if (!empty($existing_img) && strpos($existing_img, 'http') === false) {
                                        $img_name = basename($existing_img);
                                    }
                                }
                                if (empty($img_name)) {
                                    $img_name = find_closest_image($props['name'] ?? '');
                                }
                                $img_url = !empty($img_name) 
                                    ? "https://raw.githubusercontent.com/akkim-djenadi/LPC-final-/main/images_commerces/" . urlencode($img_name)
                                    : "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=150&q=80"; // fallback
                            ?>
                                <tr class="hover:bg-slate-50 transition duration-100">
                                    <td class="py-4 px-4 flex items-center space-x-3">
                                        <!-- Photo Preview synced from GitHub directly -->
                                        <div class="h-14 w-14 rounded-lg bg-slate-100 border border-slate-200 overflow-hidden flex-shrink-0 relative group">
                                            <img src="<?php echo htmlspecialchars($img_url); ?>" alt="Preview" class="h-full w-full object-cover">
                                            <?php if (!empty($img_name)): ?>
                                                <div class="absolute inset-0 bg-slate-900/60 opacity-0 group-hover:opacity-100 flex items-center justify-center transition duration-150">
                                                    <a href="<?php echo htmlspecialchars($img_url); ?>" target="_blank" class="text-white text-[9px] font-bold underline">Voir Raw ↗</a>
                                                </div>
                                            <?php endif; ?>
                                        </div>
                                        <div>
                                            <p class="font-bold text-slate-800"><?php echo htmlspecialchars($props['name'] ?? 'Inconnu'); ?></p>
                                            <p class="text-[11px] text-slate-400 max-w-xs truncate"><?php echo htmlspecialchars($props['description'] ?? ''); ?></p>
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
                                        <p class="text-[11px] text-slate-500 font-medium mt-1"><?php echo htmlspecialchars($props['subcategory'] ?? '-'); ?></p>
                                        <?php if (!empty($props['phone'])): ?>
                                            <p class="text-[10px] text-slate-400 font-mono mt-0.5"><?php echo htmlspecialchars($props['phone'] ?? ''); ?></p>
                                        <?php endif; ?>
                                    </td>
                                    <td class="py-4 px-4 font-mono text-xs text-slate-600">
                                        <span class="font-bold">Lat:</span> <?php echo number_format($coords[1], 4); ?><br>
                                        <span class="font-bold">Lng:</span> <?php echo number_format($coords[0], 4); ?>
                                    </td>
                                    <td class="py-4 px-4 text-right">
                                        <div class="flex items-center justify-end space-x-2">
                                            <a href="commerces.php?edit_id=<?php echo $props['id']; ?>" class="p-1 px-3 text-xs bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold rounded-lg transition" title="Modifier">
                                                Editer
                                            </a>
                                            <form action="commerces.php" method="POST" onsubmit="return confirm('Êtes-vous sûr de vouloir supprimer cet établissement ?');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="id" value="<?php echo $props['id']; ?>">
                                                <button type="submit" class="p-1 px-3 text-xs bg-red-50 hover:bg-red-100 text-red-600 font-semibold rounded-lg transition" title="Supprimer">
                                                    Supprimer
                                                </button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            <?php endforeach; ?>
                        </tbody>
                    </table>
                </div>
            <?php else: ?>
                <div class="p-8 text-center text-slate-500 italic block">
                    Aucun commerce n'est enregistré dans votre fichier GeoJSON. Remplissez le formulaire de gauche !
                </div>
            <?php endif; ?>
        </div>
    </div>
</div>



<?php include 'footer.php'; ?>
