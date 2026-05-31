<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

$users_file = 'data/users.json';
$commerces_file = 'data/commerces.geojson';

$users = [];
if (file_exists($users_file)) {
    $users = json_decode(file_get_contents($users_file), true);
}
if (!is_array($users)) { $users = []; }

$commerces = [];
if (file_exists($commerces_file)) {
    $geojson = json_decode(file_get_contents($commerces_file), true);
    if ($geojson && isset($geojson['features'])) {
        $commerces = $geojson['features'];
    }
}

$feedback = '';
$feedback_type = 'success';

// Handle account mutations
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';
    
    if ($action === 'create_or_update') {
        $id = $_POST['id'] ?? '';
        $name = trim($_POST['name'] ?? '');
        $email = trim($_POST['email'] ?? '');
        $password = trim($_POST['password'] ?? '');
        $role = $_POST['role'] ?? 'client';
        $merchant_id = $_POST['merchant_id'] ?? '';
        if (empty($merchant_id)) { $merchant_id = null; }

        if (empty($name) || empty($email) || empty($password)) {
            $feedback = "❌ Tous les champs obligatoires (Nom, Email, Mot de passe) doivent être remplis.";
            $feedback_type = "error";
        } else {
            // Check for email uniqueness if new
            $email_exists = false;
            foreach ($users as $u) {
                if ($u['email'] === $email && $u['id'] !== $id) {
                    $email_exists = true;
                    break;
                }
            }

            if ($email_exists) {
                $feedback = "❌ Un compte avec l'adresse email '$email' existe déjà.";
                $feedback_type = "error";
            } else {
                if (!empty($id)) {
                    // Update existing
                    foreach ($users as $key => $u) {
                        if ($u['id'] === $id) {
                            $users[$key]['name'] = $name;
                            $users[$key]['email'] = $email;
                            $users[$key]['password'] = $password;
                            $users[$key]['role'] = $role;
                            $users[$key]['merchant_id'] = ($role === 'commercant') ? $merchant_id : null;
                            break;
                        }
                    }
                    $feedback = "✅ Le compte de '$name' a été mis à jour.";
                } else {
                    // Create new
                    $new_id = "usr_" . rand(100000, 999999);
                    $users[] = [
                        "id" => $new_id,
                        "name" => $name,
                        "email" => $email,
                        "password" => $password,
                        "role" => $role,
                        "merchant_id" => ($role === 'commercant') ? $merchant_id : null
                    ];
                    $feedback = "✅ Le compte de '$name' a été créé avec succès.";
                }
                file_put_contents($users_file, json_encode($users, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            }
        }
    } elseif ($action === 'delete') {
        $id = $_POST['id'] ?? '';
        $users = array_filter($users, function($u) use ($id) {
            return $u['id'] !== $id;
        });
        $users = array_values($users);
        file_put_contents($users_file, json_encode($users, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
        $feedback = "🗑️ Le compte utilisateur a bien été supprimé.";
        $feedback_type = "success";
    }
}

// Separate users by role for separate tabs
$clients = [];
$merchants = [];
$admins = [];

foreach ($users as $u) {
    if (($u['role'] ?? 'client') === 'client') {
        $clients[] = $u;
    } elseif (($u['role'] ?? '') === 'commercant') {
        $merchants[] = $u;
    } elseif (($u['role'] ?? '') === 'admin') {
        $admins[] = $u;
    }
}

// Active UI tab (clients, commercants, admins)
$active_tab = $_GET['tab'] ?? 'clients';
if (!in_array($active_tab, ['clients', 'commercants', 'admins'])) {
    $active_tab = 'clients';
}

include 'header.php';
?>

<!-- Status message -->
<?php if (!empty($feedback)): ?>
    <div id="status_toast" class="mb-6 p-4 rounded-xl border <?php echo ($feedback_type === 'success') ? 'bg-emerald-50 border-emerald-300 text-emerald-800' : 'bg-rose-50 border-rose-300 text-rose-800'; ?> flex items-center justify-between shadow-sm">
        <div class="flex items-center space-x-2">
            <span class="material-symbols-rounded"><?php echo ($feedback_type === 'success') ? 'check_circle' : 'error'; ?></span>
            <span class="text-sm font-semibold"><?php echo $feedback; ?></span>
        </div>
        <button onclick="document.getElementById('status_toast').remove();" class="text-slate-400 hover:text-slate-600 transition">
            <span class="material-symbols-rounded text-sm">close</span>
        </button>
    </div>
<?php endif; ?>

<!-- Header banner -->
<div class="mb-8">
    <h2 class="text-2xl font-bold tracking-tight text-slate-900">Gestion des Comptes</h2>
    <p class="text-xs text-slate-500">Administrez tous les comptes d'accès à l'application. Les commerçants doivent être créés et configurés ici.</p>
</div>

<!-- Tabs selector -->
<div class="flex space-x-2 border-b border-slate-200 mb-6 bg-slate-50 p-1.5 rounded-xl">
    <a href="comptes.php?tab=clients" class="flex-1 sm:flex-none text-center px-4 py-3 rounded-lg text-xs font-bold transition flex items-center justify-center space-x-2 <?php echo ($active_tab === 'clients') ? 'bg-orange-500 text-white shadow-sm' : 'text-slate-600 hover:bg-white hover:text-slate-900'; ?>">
        <span class="material-symbols-rounded text-sm">group</span>
        <span>Utilisateurs (Clients) (<?php echo count($clients); ?>)</span>
    </a>
    <a href="comptes.php?tab=commercants" class="flex-1 sm:flex-none text-center px-4 py-3 rounded-lg text-xs font-bold transition flex items-center justify-center space-x-2 <?php echo ($active_tab === 'commercants') ? 'bg-orange-500 text-white shadow-sm' : 'text-slate-600 hover:bg-white hover:text-slate-900'; ?>">
        <span class="material-symbols-rounded text-sm">storefront</span>
        <span>Commerçants (<?php echo count($merchants); ?>)</span>
    </a>
    <a href="comptes.php?tab=admins" class="flex-1 sm:flex-none text-center px-4 py-3 rounded-lg text-xs font-bold transition flex items-center justify-center space-x-2 <?php echo ($active_tab === 'admins') ? 'bg-orange-500 text-white shadow-sm' : 'text-slate-600 hover:bg-white hover:text-slate-900'; ?>">
        <span class="material-symbols-rounded text-sm">admin_panel_settings</span>
        <span>Administrateurs (<?php echo count($admins); ?>)</span>
    </a>
</div>

<!-- Creation / Edition Panel -->
<div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm mb-6">
    <h3 class="text-sm font-bold text-slate-900 mb-4 flex items-center space-x-2" id="form_title_heading">
        <span class="material-symbols-rounded text-orange-500">person_add</span>
        <span id="form_title_txt">Créer un nouveau compte <?php 
            if ($active_tab === 'clients') echo "Utilisateur (Client)";
            elseif ($active_tab === 'commercants') echo "Commerçant";
            else echo "Administrateur";
        ?></span>
    </h3>

    <form Action="comptes.php?tab=<?php echo $active_tab; ?>" method="POST" class="space-y-4" id="account_form">
        <input type="hidden" name="action" value="create_or_update">
        <input type="hidden" name="id" id="account_id" value="">
        <input type="hidden" name="role" id="account_role" value="<?php 
            if ($active_tab === 'clients') echo 'client';
            elseif ($active_tab === 'commercants') echo 'commercant';
            else echo 'admin';
        ?>">

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
                <label class="block text-xs font-bold text-slate-600 uppercase mb-1">Nom d'affichage *</label>
                <input type="text" name="name" id="account_name" required class="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-orange-500" placeholder="Ex: Jean Dupont">
            </div>
            <div>
                <label class="block text-xs font-bold text-slate-600 uppercase mb-1">Adresse Email *</label>
                <input type="email" name="email" id="account_email" required class="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-orange-500" placeholder="Ex: jean@clapas.fr">
            </div>
            <div>
                <label class="block text-xs font-bold text-slate-600 uppercase mb-1">Mot de Passe *</label>
                <input type="text" name="password" id="account_password" required class="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-orange-500" placeholder="Mot_de_passe_secret">
            </div>
        </div>

        <?php if ($active_tab === 'commercants'): ?>
            <div id="merchant_select_box" class="p-4 bg-orange-50/50 border border-orange-200 rounded-xl">
                <label class="block text-xs font-bold text-orange-700 uppercase mb-1.5 flex items-center space-x-1">
                    <span class="material-symbols-rounded text-sm">home_work</span>
                    <span>Attribuer à un commerce de la carte *</span>
                </label>
                <select name="merchant_id" id="account_merchant_id" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs focus:outline-none focus:border-orange-500">
                    <option value="">-- Sélectionnez l'établissement représenté --</option>
                    <?php foreach($commerces as $feat): 
                        $props = $feat['properties'];
                        $m_id = $props['id'] ?? '';
                    ?>
                        <option value="<?php echo htmlspecialchars($m_id); ?>">
                            [<?php echo htmlspecialchars($props['category'] ?? 'Autre'); ?>] <?php echo htmlspecialchars($props['name'] ?? 'Nom inconnu'); ?> - ID: <?php echo htmlspecialchars($m_id); ?>
                        </option>
                    <?php endforeach; ?>
                </select>
                <p class="text-[10px] text-slate-400 mt-1">Le commerçant pourra valider les coupons cadeaux scannés s'il est rattaché au bon établissement.</p>
            </div>
        <?php endif; ?>

        <div class="flex justify-end space-x-2 pt-2">
            <button type="button" onclick="resetAccountForm()" class="px-4 py-2 bg-white hover:bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-700 shadow-sm transition">
                Vider / Nouveau
            </button>
            <button type="submit" class="px-5 py-2 bg-orange-500 hover:bg-orange-600 text-white text-xs font-bold rounded-xl shadow-sm transition flex items-center space-x-1">
                <span class="material-symbols-rounded text-sm">save</span>
                <span>Sauvegarder</span>
            </button>
        </div>
    </form>
</div>

<!-- Lists presentation based on selected active tab -->
<div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
    <div class="p-5 border-b border-slate-200 bg-slate-50/50 flex justify-between items-center">
        <span class="text-sm font-bold text-slate-800">Comptes enregistrés</span>
        <span id="tab_tagline" class="text-xs text-orange-600 font-bold bg-orange-50 px-2.5 py-0.5 rounded-lg border border-orange-200/50">
            <?php 
                if ($active_tab === 'clients') echo "Utilisateurs ordinaires";
                elseif ($active_tab === 'commercants') echo "Commerçants des stands";
                else echo "Administrateurs Backoffice";
            ?>
        </span>
    </div>

    <?php 
    $active_list = [];
    if ($active_tab === 'clients') $active_list = $clients;
    elseif ($active_tab === 'commercants') $active_list = $merchants;
    else $active_list = $admins;
    
    if (!empty($active_list)): 
    ?>
        <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
                <thead>
                    <tr class="border-b border-slate-200 text-[10px] font-bold text-slate-400 uppercase tracking-wider bg-slate-50">
                        <th class="py-3 px-4">ID</th>
                        <th class="py-3 px-4">Nom d'affichage</th>
                        <th class="py-3 px-4">Identifiant (Email)</th>
                        <th class="py-3 px-4">Mot de Passe</th>
                        <?php if ($active_tab === 'commercants'): ?>
                            <th class="py-3 px-4">Établissement lié</th>
                        <?php endif; ?>
                        <th class="py-3 px-4 text-right">Actions</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100 text-sm">
                    <?php foreach ($active_list as $u): ?>
                        <tr class="hover:bg-slate-50/30">
                            <td class="py-3.5 px-4 font-mono text-xs text-slate-400"><?php echo htmlspecialchars($u['id']); ?></td>
                            <td class="py-3.5 px-4">
                                <div class="font-bold text-slate-800 text-xs flex items-center space-x-1.5">
                                    <span><?php echo htmlspecialchars($u['name']); ?></span>
                                    <?php if ($active_tab === 'admins'): ?>
                                        <span class="bg-red-50 text-red-700 text-[9px] font-bold px-1.5 py-0.2 rounded border border-red-200">ADMIN</span>
                                    <?php endif; ?>
                                </div>
                            </td>
                            <td class="py-3.5 px-4 text-xs font-medium text-slate-600"><?php echo htmlspecialchars($u['email']); ?></td>
                            <td class="py-3.5 px-4 font-mono text-xs text-slate-500"><?php echo htmlspecialchars($u['password']); ?></td>
                            <?php if ($active_tab === 'commercants'): 
                                $tied_to = "Aucun";
                                foreach($commerces as $feat) {
                                    if (($feat['properties']['id'] ?? '') === $u['merchant_id']) {
                                        $tied_to = $feat['properties']['name'];
                                        break;
                                    }
                                }
                            ?>
                                <td class="py-3.5 px-4">
                                    <span class="inline-flex px-2 py-0.5 rounded text-[10px] font-bold font-mono bg-indigo-50 text-indigo-700">
                                        <?php echo htmlspecialchars($tied_to); ?>
                                    </span>
                                    <span class="text-[9px] text-slate-400 block mt-0.5 font-mono">(ID: <?php echo htmlspecialchars($u['merchant_id'] ?? 'aucun'); ?>)</span>
                                </td>
                            <?php endif; ?>
                            <td class="py-3.5 px-4 text-right">
                                <div class="inline-flex space-x-2">
                                    <button onclick="editAccount(<?php echo htmlspecialchars(json_encode($u)); ?>)" class="py-1 px-2 text-[11px] font-bold bg-slate-100 hover:bg-slate-200 text-slate-700 border border-slate-200 rounded-lg transition flex items-center space-x-0.5">
                                        <span class="material-symbols-rounded text-sm">edit</span>
                                        <span>Modifier</span>
                                    </button>
                                    <form action="comptes.php?tab=<?php echo $active_tab; ?>" method="POST" onsubmit="return confirm('Supprimer ce compte définitivement ?');" class="inline-block">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="<?php echo htmlspecialchars($u['id']); ?>">
                                        <button type="submit" class="py-1 px-2 text-[11px] font-bold bg-rose-50 hover:bg-rose-100 text-rose-700 border border-rose-200 rounded-lg transition flex items-center space-x-0.5">
                                            <span class="material-symbols-rounded text-sm">delete</span>
                                            <span>Supprimer</span>
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
        <div class="p-12 text-center text-slate-500 italic block">
            <span class="material-symbols-rounded text-4xl block text-slate-300 mb-2">person_off</span>
            Aucun compte n'appartient à ce groupe pour le moment.
        </div>
    <?php endif; ?>
</div>

<script>
function editAccount(userObj) {
    document.getElementById('account_id').value = userObj.id || '';
    document.getElementById('account_name').value = userObj.name || '';
    document.getElementById('account_email').value = userObj.email || '';
    document.getElementById('account_password').value = userObj.password || '';
    document.getElementById('account_role').value = userObj.role || 'client';
    
    const merchantSelect = document.getElementById('account_merchant_id');
    if (merchantSelect) {
        merchantSelect.value = userObj.merchant_id || '';
    }
    
    // Smooth scroll to form
    document.getElementById('account_form').scrollIntoView({ behavior: 'smooth' });
    
    // Change edit title
    document.getElementById('form_title_txt').innerText = "Modifier la fiche de " + (userObj.name || '');
    document.getElementById('form_title_heading').classList.add('text-orange-600');
}

function resetAccountForm() {
    document.getElementById('account_id').value = '';
    document.getElementById('account_name').value = '';
    document.getElementById('account_email').value = '';
    document.getElementById('account_password').value = '';
    
    const merchantSelect = document.getElementById('account_merchant_id');
    if (merchantSelect) {
        merchantSelect.value = '';
    }
    
    document.getElementById('form_title_txt').innerText = "Créer un nouveau compte " + (
        "<?php echo $active_tab; ?>" === 'clients' ? "Utilisateur (Client)" : ("<?php echo $active_tab; ?>" === 'commercants' ? "Commerçant" : "Administrateur")
    );
    document.getElementById('form_title_heading').classList.remove('text-orange-600');
}
</script>

<?php include 'footer.php'; ?>
