<?php
$coupons_file = 'data/coupons.json';
$commerces_file = 'data/commerces.geojson';

$coupons = [];
$commerces = [];
$feedback_msg = "";
$feedback_type = "success";

// Load data files
if (file_exists($coupons_file)) {
    $coupons = json_decode(file_get_contents($coupons_file), true);
}
if (file_exists($commerces_file)) {
    $geojson = json_decode(file_get_contents($commerces_file), true);
    if ($geojson && isset($geojson['features'])) {
        foreach ($geojson['features'] as $f) {
            if (isset($f['properties']['id'])) {
                $commerces[$f['properties']['id']] = $f['properties']['name'] ?? 'Nom inconnu';
            }
        }
    }
}

// HANDLE SUBMISSIONS: ADD / EDIT
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (isset($_POST['action']) && ($_POST['action'] === 'add' || $_POST['action'] === 'edit')) {
        $merchant_id = trim($_POST['merchant_id'] ?? '');
        $title = trim($_POST['title'] ?? '');
        $description = trim($_POST['description'] ?? '');
        $quota = intval($_POST['quota'] ?? 10);
        $active = isset($_POST['active']) && $_POST['active'] == '1' ? true : false;
        
        if (empty($title) || empty($merchant_id)) {
            $feedback_msg = "❌ Veuillez spécifier le titre de l'avantage et l'associer à un commerçant.";
            $feedback_type = "error";
        } else {
            if ($_POST['action'] === 'add') {
                $id = 'coup_' . rand(1000, 9999);
                $new_coupon = [
                    "id" => $id,
                    "merchant_id" => $merchant_id,
                    "title" => $title,
                    "description" => $description,
                    "quota" => $quota,
                    "active" => $active
                ];
                $coupons[] = $new_coupon;
                $feedback_msg = "🎟️ Le coupon d'avantage '$title' a été créé et associé !";
            } else {
                // Edit
                $id = $_POST['id'] ?? '';
                $found = false;
                foreach ($coupons as $key => $c) {
                    if ($c['id'] === $id) {
                        $coupons[$key]['merchant_id'] = $merchant_id;
                        $coupons[$key]['title'] = $title;
                        $coupons[$key]['description'] = $description;
                        $coupons[$key]['quota'] = $quota;
                        $coupons[$key]['active'] = $active;
                        $found = true;
                        break;
                    }
                }
                if ($found) {
                    $feedback_msg = "✅ Le coupon '$title' a été modifié avec succès.";
                } else {
                    $feedback_msg = "❌ Impossible de trouver ce coupon à modifier.";
                    $feedback_type = "error";
                }
            }
            
            // Save updated coupons JSON
            file_put_contents($coupons_file, json_encode($coupons, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
        }
    }
    
    // HANDLE DELETE
    if (isset($_POST['action']) && $_POST['action'] === 'delete') {
        $id = $_POST['id'] ?? '';
        $initial_count = count($coupons);
        $coupons = array_filter($coupons, function($c) use ($id) {
            return $c['id'] !== $id;
        });
        
        if (count($coupons) < $initial_count) {
            file_put_contents($coupons_file, json_encode(array_values($coupons), JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            $feedback_msg = "🗑️ Le coupon d'avantage a été annulé avec succès.";
        } else {
            $feedback_msg = "❌ Une erreur s'est produite lors de la suppression.";
            $feedback_type = "error";
        }
    }
}

// Prefill Form for Edit Mode
$edit_mode = false;
$edit_item = ['id' => '', 'merchant_id' => '', 'title' => '', 'description' => '', 'quota' => 10, 'active' => true];
if (isset($_GET['edit_id'])) {
    $edit_id = $_GET['edit_id'];
    foreach ($coupons as $c) {
        if ($c['id'] === $edit_id) {
            $edit_mode = true;
            $edit_item = [
                'id' => $c['id'],
                'merchant_id' => $c['merchant_id'] ?? '',
                'title' => $c['title'] ?? '',
                'description' => $c['description'] ?? '',
                'quota' => intval($c['quota'] ?? 10),
                'active' => (bool)($c['active'] ?? true)
            ];
            break;
        }
    }
}

include 'header.php';
?>

<?php if (!empty($feedback_msg)): ?>
    <div class="auto-dismiss mb-6 p-4 rounded-xl border <?php echo ($feedback_type === 'success') ? 'bg-emerald-50 border-emerald-300 text-emerald-800' : 'bg-rose-50 border-rose-300 text-rose-800'; ?> flex items-center space-x-2">
        <span class="material-symbols-rounded"><?php echo ($feedback_type === 'success') ? 'check_circle' : 'error'; ?></span>
        <span class="text-sm font-semibold"><?php echo $feedback_msg; ?></span>
    </div>
<?php endif; ?>

<div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
    <!-- Form Panel to Add/Edit Coupon -->
    <div class="lg:col-span-1">
        <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm sticky top-6">
            <h3 class="text-lg font-bold text-slate-900 mb-4 flex items-center space-x-2">
                <span class="material-symbols-rounded text-orange-500">confirmation_number</span>
                <span><?php echo $edit_mode ? "Éditer le bon cadeau" : "Créer un coupon avantage"; ?></span>
            </h3>

            <?php if (empty($commerces)): ?>
                <div class="p-4 bg-amber-50 rounded-xl border border-amber-200 text-xs text-amber-800 leading-normal">
                    ⚠️ <strong>Attention :</strong> Aucun établissement n'est référencé pour le moment dans votre fichier GeoJSON. <br>
                    Veuillez d'abord en créer un dans l'onglet <a href="commerces.php" class="font-bold underline text-amber-950">Commerces & Carto</a> afin de pouvoir associer vos tickets de cadeaux !
                </div>
            <?php else: ?>
                <form action="coupons.php" method="POST" class="space-y-4">
                    <input type="hidden" name="action" value="<?php echo $edit_mode ? 'edit' : 'add'; ?>">
                    <?php if ($edit_mode): ?>
                        <input type="hidden" name="id" value="<?php echo htmlspecialchars($edit_item['id']); ?>">
                    <?php endif; ?>

                    <div>
                        <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Établissement rattaché *</label>
                        <select name="merchant_id" required class="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:border-orange-500 focus:outline-none bg-white">
                            <option value="">-- Choisir un magasin --</option>
                            <?php foreach ($commerces as $mId => $mName): ?>
                                <option value="<?php echo htmlspecialchars($mId); ?>" <?php echo ($edit_item['merchant_id'] === $mId) ? 'selected' : ''; ?>>
                                    <?php echo htmlspecialchars($mName); ?>
                                </option>
                            <?php endforeach; ?>
                        </select>
                    </div>

                    <div>
                        <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Avantage en un coup d'œil *</label>
                        <input type="text" name="title" required class="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:border-orange-500 focus:outline-none" placeholder="Ex: Un cookie offert au choix 🍪" value="<?php echo htmlspecialchars($edit_item['title']); ?>">
                    </div>

                    <div>
                        <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Termes & Conditions (Détail)</label>
                        <textarea name="description" rows="3" class="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:border-orange-500 focus:outline-none" placeholder="Ex: Offert pour tout menu acheté. Valable uniquement les mardis soirs."><?php echo htmlspecialchars($edit_item['description']); ?></textarea>
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Nombre (Quota) *</label>
                            <input type="number" name="quota" required min="1" max="1000" class="w-full px-4 py-2 rounded-lg border border-slate-200 text-sm focus:border-orange-500 focus:outline-none font-mono" value="<?php echo htmlspecialchars($edit_item['quota']); ?>">
                        </div>
                        <div>
                            <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Disponibilité</label>
                            <select name="active" class="w-full px-4 py-2 rounded-lg border border-slate-200 text-sm focus:border-orange-500 focus:outline-none bg-white">
                                <option value="1" <?php echo $edit_item['active'] ? 'selected' : ''; ?>>Disponible</option>
                                <option value="0" <?php echo !$edit_item['active'] ? 'selected' : ''; ?>>Désactivé</option>
                            </select>
                        </div>
                    </div>

                    <div class="pt-2 flex space-x-2">
                        <button type="submit" class="flex-1 py-3 px-4 rounded-xl bg-orange-500 hover:bg-orange-600 font-bold text-white text-sm transition">
                            <?php echo $edit_mode ? 'Enregistrer' : 'Créer le Ticket'; ?>
                        </button>
                        <?php if ($edit_mode): ?>
                            <a href="coupons.php" class="py-3 px-4 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-sm font-semibold transition">Retour</a>
                        <?php endif; ?>
                    </div>
                </form>
            <?php endif; ?>
        </div>
    </div>

    <!-- Right Side: Existing Coupons Table & Status Tracker -->
    <div class="lg:col-span-2 space-y-6">
        <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
            <h3 class="text-lg font-bold text-slate-900 mb-1">Vos tickets avantage en cours</h3>
            <p class="text-xs text-slate-500 mb-6">Attribution et limites de récupération par commerce</p>

            <?php if (!empty($coupons)): ?>
                <div class="overflow-x-auto">
                    <table class="w-full text-left border-collapse">
                        <thead>
                            <tr class="border-b border-slate-200 text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                                <th class="py-3 px-4">Commerce cible</th>
                                <th class="py-3 px-4">Description Avantage</th>
                                <th class="py-3 px-4 text-center">Quota Restant</th>
                                <th class="py-3 px-4 text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-slate-100 text-sm">
                            <?php foreach ($coupons as $c): 
                                $associated_shop = isset($commerces[$c['merchant_id']]) ? $commerces[$c['merchant_id']] : "Établissement inconnu (ID: {$c['merchant_id']})";
                            ?>
                                <tr class="hover:bg-slate-50 transition">
                                    <td class="py-4 px-4 font-bold text-slate-900">
                                        <?php echo htmlspecialchars($associated_shop); ?>
                                    </td>
                                    <td class="py-4 px-4">
                                        <p class="font-bold text-orange-600 text-xs"><?php echo htmlspecialchars($c['title'] ?? ''); ?></p>
                                        <p class="text-[11px] text-slate-400 max-w-xs mt-0.5 leading-relaxed"><?php echo htmlspecialchars($c['description'] ?? ''); ?></p>
                                    </td>
                                    <td class="py-4 px-4 text-center">
                                        <span class="inline-flex px-3 py-1 font-mono text-xs font-bold rounded-full bg-slate-100 text-slate-700">
                                            <?php echo intval($c['quota'] ?? 0); ?>
                                        </span>
                                    </td>
                                    <td class="py-4 px-4 text-right">
                                        <div class="flex items-center justify-end space-x-2">
                                            <a href="coupons.php?edit_id=<?php echo $c['id']; ?>" class="p-1 px-3 text-xs bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold rounded-lg transition">Editer</a>
                                            <form action="coupons.php" method="POST" onsubmit="return confirm('Voulez-vous supprimer ce coupon ?');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="id" value="<?php echo $c['id']; ?>">
                                                <button type="submit" class="p-1 px-3 text-xs bg-red-50 hover:bg-red-100 text-red-600 font-semibold rounded-lg transition">Supprimer</button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            <?php endforeach; ?>
                        </tbody>
                    </table>
                </div>
            <?php else: ?>
                <div class="p-8 text-center text-slate-500 italic">Aucun coupon d'avantage n'a encore été mis en jeu. Créez-en un avec le formulaire.</div>
            <?php endif; ?>
        </div>
    </div>
</div>

<?php include 'footer.php'; ?>
