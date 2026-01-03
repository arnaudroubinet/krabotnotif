# TLDR
```
services:
  krabotnotif:
    container_name: krabotnotif
    image: arnaudroubinet/krabotnotif:latest-jvm
    ports:
      - 8080:8080
    environment:
      DISCORD_HOOK : Url de votre webhook
      KRALAND_USER : Votre user kraland
      KRALAND_PASSWORD : Votre password kraland
      # Optional: JVM memory settings for containers
      JAVA_OPTS: "-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"
    restart: unless-stopped
```

# Memory Management et Kubernetes

KrabotNotif inclut des fonctionnalités avancées de gestion de la mémoire pour les déploiements Kubernetes :

- **Garbage Collection programmé** : Exécution horaire configurable pour prévenir les fuites mémoire
- **Health Check mémoire** : Probe Kubernetes qui surveille l'utilisation mémoire
- **Gestion OutOfMemoryError** : Logging détaillé en cas de problèmes mémoire
- **Seuils d'alerte** : Avertissements à 80%, critique à 90%

Pour les déploiements Kubernetes, consultez [KUBERNETES.md](KUBERNETES.md) pour la configuration détaillée.

### Variables d'environnement pour la mémoire

```bash
# Planification du GC (défaut: toutes les heures)
JOB_GC_SCHEDULER_CRON=0 0 * ? * *

# Seuils de mémoire (défaut: 80% warning, 90% critical)
MEMORY_WARNING_THRESHOLD=80
MEMORY_CRITICAL_THRESHOLD=90

# Configuration JVM recommandée pour containers
JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport"
```

# TLDR (English)
```
services:
  krabotnotif:
    container_name: krabotnotif
    image: arnaudroubinet/krabotnotif:latest-jvm
    ports:
      - 8080:8080
    environment:
      DISCORD_HOOK : Your webhook URL
      KRALAND_USER : Your kraland username
      KRALAND_PASSWORD : Your kraland password
      # Optional: JVM memory settings for containers
      JAVA_OPTS: "-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"
    restart: unless-stopped
```

# Personnalisation

Krabot notif est une application Quarkus utilisant les webhook discord.

Elle va :
- Se connecter à www.kraland.org
- Vérifier toutes les minutes si vous avez reçu un message dans votre rapport ou un nouveau kramail
- Vous envoyez une notification sur discord le cas échéant
- Elle ne renverra pas deux fois la même notification pour le même événement sauf si vous relancez le robot
- /!\ Si vous recevez un nouveau message (notif et non kramail) sur kraland entre votre lecture et le scan, le bot ne pourras pas la détecter.

Pour la configurer, vous devez passer les variables d'environnement suivantes :
```
DISCORD_HOOK : Url de votre webhook
KRALAND_USER : Votre user kraland
KRALAND_PASSWORD : Votre password kraland
```
Vous pouvez également surcharger ces variables :
```
DISCORD_AVATAR_URL : L'url de l'avatar du user qui poste le message
DISCORD_USER : Le nom du user qui poste le message
DISCORD_KRAMAIL_MESSAGE : Le message en cas de réception de kramail
DISCORD_NOTIFICATION_MESSAGE : Le message en cas de notification
DISCORD_FIRST_MESSAGE : Le message lors de l'initialisation du scanner
DISCORD_LAST_MESSAGE : Le message lors de l'extinction du scanner
DISCORD_RELEASE_MESSAGE : Le message lors de la publication d'une nouvelle version
DISCORD_ERROR_PREFIX_MESSAGE: Le message en prefix d'une exception
JOB_KRALAND_SCHEDULER_EVERY : La récurrence du job analysant kraland, la valeur par défaut est 60s
JOB_GITHUB_SCHEDULER_CRON : La cron du job analysant github, la valeur par défaut est 0 0 11 ? * * * (Tous les jours à 11h00:00)
JOB_GC_SCHEDULER_CRON : La cron du job de garbage collection, la valeur par défaut est 0 0 * ? * * (À la minute 0 de chaque heure)
MEMORY_WARNING_THRESHOLD : Seuil d'avertissement mémoire en pourcentage, la valeur par défaut est 80
MEMORY_CRITICAL_THRESHOLD : Seuil critique mémoire en pourcentage, la valeur par défaut est 90
```
Pour DISCORD_KRAMAIL_MESSAGE, vous pouvez rajouter les balises suivantes qui seront remplacées par les valeurs du message :

- *originator* : l'expéditeur du kramail (par ex. l'utilisateur qui vous a envoyé le message)
- *title* : le sujet / le titre du kramail
- *recipient* : le destinataire du kramail (votre pseudo)

Exemple (template par défaut) :

```
📬 Kramail pour *recipient* de *originator*: "*title*"
```

Assurez-vous d'échapper ou de citer correctement les caractères spéciaux si nécessaire.

# Deprecation et remplacement :
```
JOB_SCHEDULER_EVERY -> JOB_KRALAND_SCHEDULER_EVERY
```

# Versionning
Le numéro de version applique le [semver](https://semver.org/lang/fr/).  
Étant donné un numéro de version MAJEUR.MINEUR.CORRECTIF, il faut incrémenter :

    le numéro de version MAJEUR quand il y a des changements non rétrocompatibles,
    le numéro de version MINEUR quand il y a des ajouts de fonctionnalités rétrocompatibles,
    le numéro de version de CORRECTIF quand il y a des corrections d’anomalies rétrocompatibles.

## Processus de release

Le processus de release est géré par un workflow GitHub Actions unique :

**Increment Version, Tag and Release** (`.github/workflows/increment-version.yml`)
   - Déclenché manuellement via l'interface GitHub Actions
   - Permet de choisir le type d'incrémentation : `major`, `minor`, ou `patch`
   - Récupère la dernière version taguée
   - Incrémente automatiquement le numéro de version selon le type choisi
   - Met à jour le fichier `pom.xml` avec la nouvelle version
   - Commit et pousse le changement de version
   - Crée et pousse le nouveau tag (format `vX.Y.Z`)
   - Build les images Docker multi-architecture (JVM et native)
   - Pousse les images vers DockerHub avec les tags `latest` et la version
   - Exécute les scans de sécurité Trivy
   - Crée une release GitHub avec notes de version auto-générées

**Note :** Le workflow `release.yml` existe toujours et se déclenche automatiquement lors de la création manuelle d'un tag (format `v*`), mais n'est plus utilisé par le processus de release automatique.

### Comment créer une nouvelle release :

1. Aller dans l'onglet "Actions" du repository GitHub
2. Sélectionner le workflow "Increment Version, Tag and Release"
3. Cliquer sur "Run workflow"
4. Choisir le type d'incrémentation (patch par défaut)
5. Le workflow exécutera automatiquement toutes les étapes de release

# Docker compose
Le fichier docker compose suivant est fait pour s'executer sous portainer en utilisant son système de variable d'environnement. remplacez stack.env par votre fichier de variables ou passer lui directement les variables en remplaçant "env_file:" par "environment: "
```
services:
  krabotnotif:
    container_name: krabotnotif
    image: arnaudroubinet/krabotnotif:latest-jvm
    ports:
      - 8080:8080
    env_file:
      - stack.env
    restart: unless-stopped
```

# Typologie des tags:

## Tous les tags existent en : 
- xxx (Pour la version JVM)
- xxx-native (Pour la version native)
- xxx étant latest ou la version de release (v1.1.1)
