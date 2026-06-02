<?php
// Enable explicit error reporting for diagnostics
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

$commerces_file = 'data/commerces.geojson';
$feedback_msg = "";
$feedback_type = "success";

// HANDLE POST ACTIONS
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    
    // 1. IMPORT GEOJSON FILE
    if (isset($_FILES['geojson_file'])) {
        if ($_FILES['geojson_file']['error'] === UPLOAD_ERR_OK) {
            $tmp_name = $_FILES['geojson_file']['tmp_name'];
            $content = file_get_contents($tmp_name);
            $decoded = json_decode($content, true);
            
            if ($decoded === null) {
                $feedback_msg = "❌ Échec de l'import : Le fichier importé n'est pas un JSON valide.";
                $feedback_type = "error";
            } elseif (!isset($decoded['type']) || $decoded['type'] !== 'FeatureCollection') {
                $feedback_msg = "❌ Échec de l'import : Le fichier doit être une FeatureCollection GeoJSON valide (contenant l'attribut \"type\": \"FeatureCollection\").";
                $feedback_type = "error";
            } else {
                // Ensure features list is present
                if (!isset($decoded['features'])) {
                    $decoded['features'] = [];
                }
                
                if (file_put_contents($commerces_file, json_encode($decoded, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE))) {
                    $features_count = count($decoded['features']);
                    $feedback_msg = "✅ Base de données mise à jour avec succès ! $features_count commerces ont été importés.";
                    $feedback_type = "success";
                } else {
                    $feedback_msg = "❌ Échec de l'import : Impossible d'écrire dans le fichier data/commerces.geojson.";
                    $feedback_type = "error";
                }
            }
        } else {
            $feedback_msg = "❌ Échec de l'import : Erreur de transfert du fichier (code d'erreur : " . $_FILES['geojson_file']['error'] . ")";
            $feedback_type = "error";
        }
    }
    
    // 2. EMPTY DATABASE
    if (isset($_POST['action']) && $_POST['action'] === 'clear_database') {
        $empty_geojson = [
            "type" => "FeatureCollection",
            "features" => []
        ];
        
        if (file_put_contents($commerces_file, json_encode($empty_geojson, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE))) {
            $feedback_msg = "🗑️ La base de données des commerces a été vidée avec succès. Tous les établissements ont été retirés.";
            $feedback_type = "success";
        } else {
            $feedback_msg = "❌ Impossible de vider la base de données : Problème d'accès en écriture au fichier.";
            $feedback_type = "error";
        }
    }
}

// Read current count for UI preview status
$commerces_count = 0;
if (file_exists($commerces_file)) {
    $geojson = json_decode(file_get_contents($commerces_file), true);
    if ($geojson && isset($geojson['features'])) {
        $commerces_count = count($geojson['features']);
    }
}

include 'header.php';
?>

<!-- Title Banner -->
<div class="mb-8">
    <h2 class="text-2xl font-extrabold text-slate-900 tracking-tight">Gestion & Import de Données</h2>
    <p class="text-sm text-slate-500 mt-1">Configurez globalement la base de données des commerces de Montpellier Écusson sans programmation complexe.</p>
</div>

<!-- Feedback Messages -->
<?php if (!empty($feedback_msg)): ?>
    <div id="alert_box" class="mb-6 p-4 rounded-xl border <?php echo ($feedback_type === 'success') ? 'bg-emerald-50 border-emerald-300 text-emerald-800' : 'bg-rose-50 border-rose-300 text-rose-800'; ?> flex items-center justify-between transition duration-200">
        <div class="flex items-center space-x-2">
            <span class="material-symbols-rounded <?php echo ($feedback_type === 'success') ? 'text-emerald-500' : 'text-rose-500'; ?>">
                <?php echo ($feedback_type === 'success') ? 'check_circle' : 'error'; ?>
            </span>
            <span class="text-xs font-semibold"><?php echo htmlspecialchars($feedback_msg); ?></span>
        </div>
        <button onclick="document.getElementById('alert_box').remove();" class="text-slate-400 hover:text-slate-600 transition">
            <span class="material-symbols-rounded text-sm">close</span>
        </button>
    </div>
<?php endif; ?>

<div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
    
    <!-- Left column: Database status and direct download backup -->
    <div class="lg:col-span-1 space-y-6">
        <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
            <h3 class="text-sm font-bold text-slate-900 uppercase tracking-wider mb-4 flex items-center space-x-2">
                <span class="material-symbols-rounded text-orange-500">database</span>
                <span>État de la base (GeoJSON)</span>
            </h3>
            
            <div class="space-y-4">
                <div class="flex items-center justify-between p-4 bg-slate-50 rounded-xl border border-slate-100">
                    <div>
                        <span class="text-xs text-slate-500 block">Commerces enregistrés</span>
                        <span class="text-2xl font-bold text-slate-900"><?php echo $commerces_count; ?></span>
                    </div>
                    <div class="h-10 w-10 text-orange-500 bg-orange-500/10 rounded-xl flex items-center justify-center">
                        <span class="material-symbols-rounded">storefront</span>
                    </div>
                </div>
                
                <div class="text-xs text-slate-500 leading-relaxed bg-orange-50/50 p-4 border border-orange-100/50 rounded-xl">
                    <p class="font-semibold text-orange-900 mb-1 flex items-center">
                        <span class="material-symbols-rounded text-sm mr-1">info</span>
                        Stockage par Fichier Plat
                    </p>
                    Vos commerces partenaires sont stockés dynamiquement dans le fichier local <code class="bg-white/80 border border-slate-200 px-1 py-0.5 rounded font-mono text-[10px] text-slate-700">backoffice/data/commerces.geojson</code>. Vous pouvez le copier ou le télécharger à tout moment pour en faire une sauvegarde.
                </div>
                
                <!-- Quick Download Backup Link -->
                <a href="index.php?download=commerces" class="flex items-center justify-center space-x-2 w-full py-3 px-4 bg-white border border-slate-200 hover:border-orange-500 hover:text-orange-600 text-slate-700 text-xs font-bold rounded-xl shadow-sm transition">
                    <span class="material-symbols-rounded text-sm">download</span>
                    <span>Télécharger la sauvegarde actuelle</span>
                </a>
            </div>
        </div>
    </div>
    
    <!-- Right column: Import and Empty operations -->
    <div class="lg:col-span-2 space-y-6">
        
        <!-- Import Form Card -->
        <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
            <h3 class="text-base font-bold text-slate-900 mb-2 flex items-center space-x-2">
                <span class="material-symbols-rounded text-orange-500">cloud_upload</span>
                <span>Importer un fichier GeoJSON (.geojson / .json)</span>
            </h3>
            <p class="text-xs text-slate-500 mb-6 leading-relaxed">
                Importez ou remplacez l'intégralité de la base de données. Le fichier importé doit contenir un format GeoJSON valide de type <code class="bg-slate-100 px-1 py-0.5 rounded font-mono text-[10px] text-slate-700">FeatureCollection</code>.
            </p>
            
            <form action="donnees.php" method="POST" enctype="multipart/form-data" class="space-y-4">
                <div class="border-2 border-dashed border-slate-200 rounded-xl p-6 text-center hover:border-orange-400 transition cursor-pointer relative" onclick="document.getElementById('donnees_file_input').click()">
                    <input type="file" name="geojson_file" accept=".geojson,.json" required class="hidden" id="donnees_file_input" onchange="updateDonneesFileName(this)">
                    <div class="space-y-2">
                        <div class="h-12 w-12 bg-slate-50 rounded-full flex items-center justify-center mx-auto text-slate-400">
                            <span class="material-symbols-rounded text-2xl">upload_file</span>
                        </div>
                        <div class="text-sm font-semibold text-slate-700" id="donnees_file_label">Glissez-déposez ou cliquez pour téléverser votre GeoJSON</div>
                        <p class="text-[11px] text-slate-400">Taille maximale : 2 Mo</p>
                    </div>
                </div>
                
                <div class="flex justify-end pt-2">
                    <button type="submit" class="px-6 py-3 bg-orange-500 hover:bg-orange-600 text-white font-bold text-xs rounded-xl shadow-sm transition flex items-center space-x-1.5">
                        <span class="material-symbols-rounded text-sm">cloud_upload</span>
                        <span>Lancer l'importation directe</span>
                    </button>
                </div>
            </form>
        </div>
        
        <!-- Clear Database Card -->
        <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
            <h3 class="text-base font-bold text-slate-900 mb-2 flex items-center space-x-2">
                <span class="material-symbols-rounded text-rose-500">delete_forever</span>
                <span>Vider la base de données des commerces</span>
            </h3>
            <p class="text-xs text-slate-500 mb-6 leading-relaxed">
                Cette opération videra intégralement la base de données active en retirant tous les établissements enregistrés. Cette action ne supprime pas les coupons ou d'autres configurations de jeu, mais affectera tous les commerces de l'application mobile.
            </p>
            
            <div class="p-4 bg-rose-50 rounded-xl border border-rose-100 flex items-start space-x-3 mb-6">
                <span class="text-xl">⚠️</span>
                <div>
                    <h4 class="text-xs font-bold text-rose-900">Avertissement de sécurité</h4>
                    <p class="text-[11px] text-rose-800 mt-0.5 leading-relaxed">Cette opération est irréversible. Pour éviter de perdre vos données définitivement, veuillez vous assurer d'avoir déjà téléchargé une sauvegarde de votre base avant de continuer.</p>
                </div>
            </div>
            
            <form action="donnees.php" method="POST" id="clear_db_form" class="flex justify-end">
                <input type="hidden" name="action" value="clear_database">
                <button type="button" onclick="confirmDatabaseClear()" class="px-6 py-3 bg-red-600 hover:bg-red-700 text-white font-bold text-xs rounded-xl shadow-sm transition flex items-center space-x-1.5">
                    <span class="material-symbols-rounded text-sm">delete_forever</span>
                    <span>Vider l'intégralité des commerces</span>
                </button>
            </form>
        </div>
        
    </div>
</div>

<script>
function updateDonneesFileName(input) {
    const txtEl = document.getElementById('donnees_file_label');
    if (input.files && input.files[0]) {
        txtEl.textContent = "Fichier sélectionné : " + input.files[0].name;
    } else {
        txtEl.textContent = "Glissez-déposez ou cliquez pour téléverser votre GeoJSON";
    }
}

function confirmDatabaseClear() {
    const confirmation1 = confirm("⚠️ ATTENTION : Êtes-vous sûr de vouloir vider l'ENTIÈRETÉ de la base de données des commerces ? Tous les partenaires référencés sur l'application de Montpellier seront supprimés de la carte.");
    if (confirmation1) {
        const confirmation2 = confirm("🚨 SÉCURITÉ SUPPLÉMENTAIRE : Confirmez-vous à nouveau cette action destructrice ? (Il est fortement recommandé d'avoir téléchargé un backup)");
        if (confirmation2) {
            document.getElementById('clear_db_form').submit();
        }
    }
}
</script>

<?php include 'footer.php'; ?>
