<?php
// Load current databases dynamically
$commerces_file = 'data/commerces.geojson';
$coupons_file = 'data/coupons.json';
$jeux_file = 'data/jeux.json';

$commerces_count = 0;
$categories_count = [];
if (file_exists($commerces_file)) {
    $geojson = json_decode(file_get_contents($commerces_file), true);
    if ($geojson && isset($geojson['features'])) {
        $commerces_count = count($geojson['features']);
        foreach ($geojson['features'] as $f) {
            $cat = $f['properties']['category'] ?? 'Autre';
            $categories_count[$cat] = ($categories_count[$cat] ?? 0) + 1;
        }
    }
}

$coupons_count = 0;
$total_quota = 0;
if (file_exists($coupons_file)) {
    $coupons = json_decode(file_get_contents($coupons_file), true);
    if ($coupons) {
        $coupons_count = count($coupons);
        foreach ($coupons as $c) {
            $total_quota += $c['quota'] ?? 0;
        }
    }
}

$jeux_count = 0;
$riddles_count = 0;
if (file_exists($jeux_file)) {
    $jeux = json_decode(file_get_contents($jeux_file), true);
    if ($jeux) {
        $jeux_count = count($jeux);
        foreach ($jeux as $j) {
            if (isset($j['enigma']) && !empty($j['enigma'])) {
                $riddles_count++;
            }
        }
    }
}

include 'header.php';
?>

<!-- Home Greeting & Banner -->
<div class="mb-8">
    <div class="bg-gradient-to-r from-slate-900 via-slate-800 to-orange-950 p-8 rounded-2xl text-white shadow-xl relative overflow-hidden">
        <div class="relative z-10 max-w-2xl">
            <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-orange-500/20 text-orange-400 border border-orange-500/30 mb-4">☀️ Edition Estivales de Montpellier</span>
            <h2 class="text-3xl font-extrabold tracking-tight mb-2">Bienvenue sur votre Espace d'Administration !</h2>
            <p class="text-slate-300 text-sm leading-relaxed mb-6">
                Gérez facilement vos établissements via GeoJSON, configurez les énigmes géolocalisées avec calcul du temps, et liez vos images de commerces directement hébergées sur votre GitHub.
            </p>
            <div class="flex flex-wrap gap-4">
                <a href="commerces.php" class="px-5 py-2.5 rounded-xl bg-orange-500 hover:bg-orange-600 font-semibold text-sm transition duration-150 flex items-center space-x-2">
                    <span class="material-symbols-rounded text-lg">add_location_alt</span>
                    <span>Ajouter un Commerce</span>
                </a>
                <a href="jeux.php" class="px-5 py-2.5 rounded-xl bg-slate-700 hover:bg-slate-600 font-semibold text-sm transition duration-150 flex items-center space-x-2">
                    <span class="material-symbols-rounded text-lg">explore</span>
                    <span>Configurer le Jeu de Piste</span>
                </a>
            </div>
        </div>
        <!-- Decorative subtle grid -->
        <div class="absolute inset-0 opacity-10 pointer-events-none bg-[radial-gradient(#f97316_1px,transparent_1px)] [background-size:16px_16px]"></div>
    </div>
</div>

<!-- Key Stat Cards Grid -->
<div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
    <!-- Card 1: Commerces GeoJSON -->
    <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex items-center justify-between">
        <div class="space-y-1">
            <span class="text-xs font-semibold text-slate-500 uppercase tracking-wider">Établissements Référencés</span>
            <h3 class="text-3xl font-bold text-slate-900"><?php echo $commerces_count; ?></h3>
            <p class="text-xs text-slate-500 italic">Disponibles dans <span class="code-font bg-slate-100 px-1 py-0.5 rounded text-[10px]/normal">commerces.geojson</span></p>
        </div>
        <div class="h-14 w-14 rounded-2xl bg-orange-500/10 text-orange-500 flex items-center justify-center">
            <span class="material-symbols-rounded text-3xl">storefront</span>
        </div>
    </div>

    <!-- Card 2: Coupons & Rewards -->
    <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex items-center justify-between">
        <div class="space-y-1">
            <span class="text-xs font-semibold text-slate-500 uppercase tracking-wider">Vouchers & Coupons d'Avantages</span>
            <h3 class="text-3xl font-bold text-slate-900"><?php echo $coupons_count; ?></h3>
            <p class="text-xs text-slate-500 italic">Cumul de <span class="font-bold text-slate-700"><?php echo $total_quota; ?></span> tickets disponibles au total</p>
        </div>
        <div class="h-14 w-14 rounded-2xl bg-cyan-500/10 text-cyan-500 flex items-center justify-center">
            <span class="material-symbols-rounded text-3xl">confirmation_number</span>
        </div>
    </div>

    <!-- Card 3: Active Interactive Games -->
    <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex items-center justify-between">
        <div class="space-y-1">
            <span class="text-xs font-semibold text-slate-500 uppercase tracking-wider">Jeux configurés sur place</span>
            <h3 class="text-3xl font-bold text-slate-900"><?php echo $jeux_count; ?></h3>
            <p class="text-xs text-slate-500 italic"><span class="font-bold text-slate-700"><?php echo $riddles_count; ?></span> énigme géolocalisée par GPS actif</p>
        </div>
        <div class="h-14 w-14 rounded-2xl bg-indigo-500/10 text-indigo-500 flex items-center justify-center">
            <span class="material-symbols-rounded text-3xl">explore</span>
        </div>
    </div>
</div>

<div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
    <!-- Left Column: Categories overview -->
    <div class="lg:col-span-1 bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
        <h4 class="text-base font-bold text-slate-900 mb-4 flex items-center space-x-2">
            <span class="material-symbols-rounded text-orange-500">category</span>
            <span>Répartition par Catégories</span>
        </h4>
        
        <?php if (!empty($categories_count)): ?>
            <div class="space-y-4">
                <?php foreach ($categories_count as $catName => $count): 
                    $pct = ($commerces_count > 0) ? round(($count / $commerces_count) * 100) : 0;
                ?>
                    <div>
                        <div class="flex justify-between text-xs font-semibold text-slate-700 mb-1">
                            <span><?php echo htmlspecialchars($catName); ?></span>
                            <span><?php echo $count; ?> (<?php echo $pct; ?>%)</span>
                        </div>
                        <div class="w-full bg-slate-100 rounded-full h-2">
                            <div class="bg-orange-500 h-2 rounded-full" style="width: <?php echo $pct; ?>%"></div>
                        </div>
                    </div>
                <?php endforeach; ?>
            </div>
        <?php else: ?>
            <p class="text-slate-500 text-xs italic">Aucune catégorie répertoriée actuellement dans votre fichier GeoJSON.</p>
        <?php endif; ?>
    </div>

    <!-- Right Column: Interactive GitHub Photo Sync Tutorial -->
    <div class="lg:col-span-2 bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
        <h4 class="text-base font-bold text-slate-900 mb-4 flex items-center space-x-2">
            <span class="material-symbols-rounded text-blue-500">sync_alt</span>
            <span>Synchronisation Image GitHub</span>
        </h4>
        <div class="text-xs text-slate-600 space-y-3 leading-relaxed">
            <p>
                Vous utilisez un répertoire GitHub dédié pour stocker les photos des commerçants de l'Écusson. Vos clients peuvent ainsi charger instantanément les visuels correspondants.
            </p>
            
            <div class="bg-slate-50 p-4 rounded-xl border border-slate-200">
                <p class="font-bold text-slate-800 mb-1.5 flex items-center">
                    <span class="material-symbols-rounded text-indigo-500 text-sm mr-1">link</span>
                    Comment ça fonctionne ?
                </p>
                <ol class="list-decimal list-inside space-y-1">
                    <li>Déposez vos images (ex: <code class="bg-slate-200 px-1 rounded font-semibold text-slate-900">vignoble_st_jean.jpg</code>) directement dans votre dossier GitHub <a href="https://github.com/akkim-djenadi/le-petit-clapas-/tree/main/images_commerces" target="_blank" class="text-blue-500 hover:underline">images_commerces</a>.</li>
                    <li>Renseignez le nom exact de ce fichier dans la fiche établissement correspondante dans ce back-office.</li>
                    <li>Le serveur génère le lien brut CDN GitHub direct :
                        <br>
                        <span class="text-[10px]/snug font-mono text-emerald-600 block mt-1 break-all">https://raw.githubusercontent.com/akkim-djenadi/le-petit-clapas-/main/images_commerces/vignoble_st_jean.jpg</span>
                    </li>
                </ol>
            </div>

            <div class="p-3 bg-amber-50 rounded-xl border border-amber-200/50 flex space-x-3 mt-4">
                <span class="text-xl">💡</span>
                <div>
                    <h5 class="font-bold text-amber-900">Pas de base de données SQL complexe requise !</h5>
                    <p class="text-[11px] text-amber-800 lead-relaxed">
                        Toutes les modifications en direct sur vos coordonnées géographiques et bons sont stockées de façon sécurisée sous forme de fichiers plats JSON et GeoJSON. Vous pouvez instantanément migrer ce dossier via votre FTP vers n'importe quel hébergeur simple PHP !
                    </p>
                </div>
            </div>
        </div>
    </div>
</div>

<?php include 'footer.php'; ?>
