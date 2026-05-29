<?php
$jeux_file = 'data/jeux.json';
$jeux = [];
$feedback_msg = "";
$feedback_type = "success";

// Read current games library
if (file_exists($jeux_file)) {
    $jeux = json_decode(file_get_contents($jeux_file), true);
}

// HANDLE UPDATE SUBMISSION
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action']) && $_POST['action'] === 'edit_game') {
    $game_id = $_POST['id'] ?? '';
    $title = trim($_POST['title'] ?? '');
    $description = trim($_POST['description'] ?? '');
    $enigma = trim($_POST['enigma'] ?? '');
    $target_lat = isset($_POST['target_lat']) ? floatval($_POST['target_lat']) : null;
    $target_lng = isset($_POST['target_lng']) ? floatval($_POST['target_lng']) : null;
    $active = isset($_POST['active']) && $_POST['active'] == '1' ? true : false;
    
    $found = false;
    foreach ($jeux as $key => $game) {
        if ($game['id'] === $game_id) {
            $jeux[$key]['title'] = $title;
            $jeux[$key]['description'] = $description;
            if ($game_id === 'game_piste_01') {
                $jeux[$key]['enigma'] = $enigma;
                $jeux[$key]['target_lat'] = $target_lat;
                $jeux[$key]['target_lng'] = $target_lng;
            }
            $jeux[$key]['active'] = $active;
            $found = true;
            break;
        }
    }
    
    if ($found) {
        file_put_contents($jeux_file, json_encode($jeux, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
        $feedback_msg = "✅ Succès ! Les paramètres du jeu '$title' ont été sauvegardés.";
    } else {
        $feedback_msg = "❌ Impossible de trouver le jeu à éditer.";
        $feedback_type = "error";
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

<div class="space-y-8">
    <!-- Game List Overview -->
    <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
        <div>
            <h3 class="text-lg font-bold text-slate-900 mb-1">🎮 Gestion des Jeux Mobiles Actifs</h3>
            <p class="text-xs text-slate-500 mb-6">Mettez en ligne des chasses au trésor et contrôlez les règles d'attribution des tickets.</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <?php foreach ($jeux as $game): 
                $is_piste = ($game['id'] === 'game_piste_01');
            ?>
                <!-- Individual Game Card -->
                <div class="p-6 rounded-2xl border <?php echo $game['active'] ? 'border-orange-500/20 bg-orange-500/5' : 'border-slate-200 bg-white'; ?> flex flex-col justify-between">
                    <div>
                        <div class="flex justify-between items-start mb-4">
                            <span class="text-3xl"><?php echo $is_piste ? '🗺️' : '🐦'; ?></span>
                            <span class="inline-flex px-2 py-0.5 rounded text-[10px]/normal font-semibold <?php echo $game['active'] ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-100 text-slate-800'; ?>">
                                <?php echo $game['active'] ? 'En ligne' : 'Inactif'; ?>
                            </span>
                        </div>
                        <h4 class="text-base font-bold text-slate-900 mb-1"><?php echo htmlspecialchars($game['title']); ?></h4>
                        <p class="text-xs text-slate-600 mb-4"><?php echo htmlspecialchars($game['description']); ?></p>
                        
                        <?php if ($is_piste): ?>
                            <!-- Specific Piste Specs -->
                            <div class="space-y-2 mt-4 pt-4 border-t border-slate-200/60 text-xs">
                                <div class="bg-white p-3 rounded-lg border border-slate-100 italic text-slate-700">
                                    <span class="font-bold text-slate-900 block mb-0.5">Énigme :</span>
                                    "<?php echo htmlspecialchars($game['enigma'] ?? ''); ?>"
                                </div>
                                <div class="grid grid-cols-2 gap-2 font-mono text-[11px] text-slate-500">
                                    <div><strong class="text-slate-700">Latitude :</strong> <?php echo htmlspecialchars($game['target_lat'] ?? ''); ?></div>
                                    <div><strong class="text-slate-700">Longitude :</strong> <?php echo htmlspecialchars($game['target_lng'] ?? ''); ?></div>
                                </div>
                            </div>
                        <?php endif; ?>
                        
                        <!-- Leaderboard display -->
                        <?php if (isset($game['best_score_seconds'])): ?>
                            <div class="mt-4 pt-3 border-t border-slate-100 flex justify-between text-xs text-slate-500">
                                <span>Record : <strong class="text-slate-800"><?php echo $game['best_score_player']; ?></strong></span>
                                <span>Temps : <strong class="text-slate-800 font-mono"><?php echo $game['best_score_seconds']; ?> sc</strong></span>
                            </div>
                        <?php endif; ?>
                    </div>

                    <div class="mt-6">
                        <a href="jeux.php?edit_id=<?php echo $game['id']; ?>" class="inline-flex w-full items-center justify-center p-2.5 rounded-xl border border-orange-500 text-xs font-bold text-orange-600 bg-white hover:bg-orange-500 hover:text-white transition cursor-pointer">
                            Configurer ce jeu ➡️
                        </a>
                    </div>
                </div>
            <?php endforeach; ?>
        </div>
    </div>

    <!-- Active Edit Editor Panel -->
    <?php if (isset($_GET['edit_id'])): 
        $edit_id = $_GET['edit_id'];
        $selected_game = null;
        foreach ($jeux as $g) {
            if ($g['id'] === $edit_id) {
                $selected_game = $g;
                break;
            }
        }
        
        if ($selected_game):
            $is_piste = ($selected_game['id'] === 'game_piste_01');
    ?>
        <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-md">
            <h4 class="text-base font-bold text-slate-900 mb-4 flex items-center space-x-2">
                <span class="material-symbols-rounded text-orange-500">settings</span>
                <span>Paramétrage de : <strong class="text-slate-950"><?php echo htmlspecialchars($selected_game['title']); ?></strong></span>
            </h4>
            
            <form action="jeux.php?edit_id=<?php echo $selected_game['id']; ?>" method="POST" class="space-y-4">
                <input type="hidden" name="action" value="edit_game">
                <input type="hidden" name="id" value="<?php echo $selected_game['id']; ?>">
                
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div class="space-y-4">
                        <div>
                            <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Titre de l'événement</label>
                            <input type="text" name="title" required class="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:border-orange-500 focus:outline-none" value="<?php echo htmlspecialchars($selected_game['title']); ?>">
                        </div>

                        <div>
                            <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Description courte du défi</label>
                            <textarea name="description" rows="3" required class="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:border-orange-500 focus:outline-none"><?php echo htmlspecialchars($selected_game['description']); ?></textarea>
                        </div>

                        <div>
                            <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Mise en ligne du jeu</label>
                            <div class="flex items-center space-x-4">
                                <label class="inline-flex items-center">
                                    <input type="radio" name="active" value="1" <?php echo $selected_game['active'] ? 'checked' : ''; ?> class="form-radio text-orange-500 focus:ring-orange-500">
                                    <span class="ml-2 text-sm text-slate-700">Actif (Visible sur l'App mobile)</span>
                                </label>
                                <label class="inline-flex items-center">
                                    <input type="radio" name="active" value="0" <?php echo !$selected_game['active'] ? 'checked' : ''; ?> class="form-radio text-orange-500 focus:ring-orange-500">
                                    <span class="ml-2 text-sm text-slate-700">Inactif (Masqué)</span>
                                </label>
                            </div>
                        </div>
                    </div>

                    <?php if ($is_piste): ?>
                        <!-- Specific Fields for localized Georiddle -->
                        <div class="space-y-4 bg-slate-50 p-5 rounded-2xl border border-slate-200">
                            <span class="text-[10px] font-bold text-orange-600 block tracking-wider uppercase">Configuration GPS de la Piste</span>
                            
                            <div>
                                <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Énigme à résoudre</label>
                                <textarea name="enigma" rows="4" class="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:border-orange-500 focus:outline-none" placeholder="Donnez des indices insolites sur un lieu de Montpellier..."><?php echo htmlspecialchars($selected_game['enigma'] ?? ''); ?></textarea>
                            </div>

                            <div class="grid grid-cols-2 gap-4">
                                <div>
                                    <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Latitude Cible *</label>
                                    <input type="text" name="target_lat" required id="inp_lat" class="w-full px-4 py-2 rounded-lg border border-slate-200 font-mono text-xs focus:border-orange-500 focus:outline-none" value="<?php echo htmlspecialchars($selected_game['target_lat'] ?? '43.6085'); ?>">
                                </div>
                                <div>
                                    <label class="block text-xs font-semibold text-slate-700 uppercase mb-1">Longitude Cible *</label>
                                    <input type="text" name="target_lng" required id="inp_lng" class="w-full px-4 py-2 rounded-lg border border-slate-200 font-mono text-xs focus:border-orange-500 focus:outline-none" value="<?php echo htmlspecialchars($selected_game['target_lng'] ?? '3.8794'); ?>">
                                </div>
                            </div>

                            <!-- Clickable pre-sets to ease editing -->
                            <div>
                                <span class="block text-[10px] font-semibold text-slate-400 uppercase tracking-wider mb-1.5">🎯 Lieux cultes (Cliquez pour injecter) :</span>
                                <div class="flex flex-wrap gap-1.5">
                                    <button type="button" onclick="setGPS(43.6085, 3.8794)" class="px-2 py-1 rounded bg-white hover:bg-slate-200 border text-[10px] font-medium text-slate-700 transition">Place de la Comédie</button>
                                    <button type="button" onclick="setGPS(43.6111, 3.8715)" class="px-2 py-1 rounded bg-white hover:bg-slate-200 border text-[10px] font-medium text-slate-700 transition">Arc de Triomphe</button>
                                    <button type="button" onclick="setGPS(43.6141, 3.8718)" class="px-2 py-1 rounded bg-white hover:bg-slate-200 border text-[10px] font-medium text-slate-700 transition">Jardin des Plantes</button>
                                    <button type="button" onclick="setGPS(43.6080, 3.8770)" class="px-2 py-1 rounded bg-white hover:bg-slate-200 border text-[10px] font-medium text-slate-700 transition">Église Saint-Roch</button>
                                </div>
                            </div>
                        </div>
                    <?php endif; ?>
                </div>

                <div class="flex justify-end space-x-3 pt-4 border-t border-slate-100">
                    <a href="jeux.php" class="py-2.5 px-4 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-sm font-semibold transition">Fermer</a>
                    <button type="submit" class="py-2.5 px-6 rounded-xl bg-orange-500 hover:bg-orange-600 text-white text-sm font-bold transition">Enregistrer la configuration</button>
                </div>
            </form>
        </div>
        
        <script>
            function setGPS(lat, lng) {
                document.getElementById('inp_lat').value = lat;
                document.getElementById('inp_lng').value = lng;
            }
        </script>
    <?php endif; endif; ?>
</div>

<?php include 'footer.php'; ?>
