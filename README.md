# KrabotNotif

Application Quarkus utilisant les webhooks Discord pour surveiller votre compte Kraland.

## Fonctionnalités

- Se connecte à www.kraland.org
- Vérifie toutes les minutes les nouveaux messages (rapport) et kramails
- Envoie une notification Discord le cas échéant
- Ne renvoie pas deux fois la même notification pour le même événement (sauf redémarrage)
- **Rappel de sommeil** : vérifie quotidiennement si l'action "Dormir" est disponible et envoie un rappel

> ⚠️ Si vous recevez un nouveau message (notif et non kramail) sur Kraland entre votre lecture et le scan, le bot ne pourra pas le détecter.

## Quick Start

```yaml
services:
  krabotnotif:
    container_name: krabotnotif
    image: arnaudroubinet/krabotnotif:latest-jvm
    ports:
      - 8080:8080
    environment:
      DISCORD_HOOK: <Url de votre webhook>
      KRALAND_USER: <Votre user kraland>
      KRALAND_PASSWORD: <Votre password kraland>
      KRABOT_BACKEND_URL: http://localhost:8080
      JOB_KRALAND_SCHEDULER_EVERY: 60s
      JOB_KRALAND_SCHEDULER_DELAY: 5m
    restart: unless-stopped
```

## Configuration

### Variables requises

| Variable | Description |
|----------|-------------|
| `DISCORD_HOOK` | URL de votre webhook Discord |
| `KRALAND_USER` | Votre identifiant Kraland |
| `KRALAND_PASSWORD` | Votre mot de passe Kraland |

### Variables optionnelles

| Variable | Description | Valeur par défaut |
|----------|-------------|-------------------|
| `DISCORD_AVATAR_URL` | URL de l'avatar du bot | `http://img.kraland.org/a/krabot.jpg` |
| `DISCORD_USER` | Nom du bot | `Krabot` |
| `DISCORD_KRAMAIL_MESSAGE` | Message pour les kramails | `📬 Kramail pour *recipient* de *originator*: "*title*"` |
| `DISCORD_NOTIFICATION_MESSAGE` | Message pour les notifications | `Hey, tu as une notification !!` |
| `DISCORD_FIRST_MESSAGE` | Message à l'initialisation | `Krabot est de retour... pour vous jouer un mauvais tour !` |
| `DISCORD_LAST_MESSAGE` | Message à l'extinction | `Je m'en vais, au revoir !` |
| `DISCORD_RELEASE_MESSAGE` | Message pour les nouvelles versions | `Une nouvelle release de KrabotNotif est disponible` |
| `DISCORD_SLEEP_MESSAGE` | Message de rappel de sommeil | `N'oublie pas de dormir` |
| `DISCORD_ERROR_PREFIX_MESSAGE` | Préfixe des messages d'erreur | `Oh no !` |
| `KRABOT_BACKEND_URL` | URL du backend Krabot | `http://localhost:8080` |
| `JOB_KRALAND_SCHEDULER_EVERY` | Récurrence du scan Kraland | `60s` |
| `JOB_KRALAND_SCHEDULER_DELAY` | Délai avant le premier scan | `5m` |
| `JOB_GITHUB_SCHEDULER_CRON` | Cron du scan GitHub | `0 0 11 ? * * *` (11h00) |
| `JOB_SLEEP_SCHEDULER_TIME` | Heure du rappel de sommeil (format HH:mm) | `20:00` |

#### Format des durées Java

Les variables `JOB_KRALAND_SCHEDULER_EVERY` et `JOB_KRALAND_SCHEDULER_DELAY` utilisent le format de durée Java :

| Suffixe | Unité | Exemple |
|---------|-------|---------|
| `s` | secondes | `60s` = 60 secondes |
| `m` | minutes | `5m` = 5 minutes |
| `h` | heures | `2h` = 2 heures |
| `d` | jours | `1d` = 1 jour |

Ces suffixes peuvent être combinés : `1h30m` = 1 heure et 30 minutes.

#### Format des expressions Cron

Les variables `JOB_GITHUB_SCHEDULER_CRON` et `JOB_GC_SCHEDULER_CRON` utilisent le format cron Quartz (6-7 champs) :

```
secondes minutes heures jour-du-mois mois jour-de-la-semaine [année]
```

| Champ | Valeurs | Caractères spéciaux |
|-------|---------|---------------------|
| Secondes | 0-59 | `, - * /` |
| Minutes | 0-59 | `, - * /` |
| Heures | 0-23 | `, - * /` |
| Jour du mois | 1-31 | `, - * ? /` |
| Mois | 1-12 ou JAN-DEC | `, - * /` |
| Jour de la semaine | 0-7 ou SUN-SAT | `, - * ? /` |

Exemples :
- `0 0 11 ? * * *` : tous les jours à 11h00
- `0 0 * ? * *` : toutes les heures

#### Template des kramails

Pour `DISCORD_KRAMAIL_MESSAGE`, utilisez ces balises :
- `*originator*` : expéditeur du kramail
- `*title*` : sujet du kramail
- `*recipient*` : destinataire (votre pseudo)

### Migration

```
JOB_SCHEDULER_EVERY → JOB_KRALAND_SCHEDULER_EVERY
```

## Docker Compose avec Portainer

Pour utiliser avec Portainer et son système de variables d'environnement :

```yaml
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

Remplacez `env_file:` par `environment:` pour passer les variables directement.

## Tags Docker

Les tags existent en deux variantes :
- `xxx` : version JVM
- `xxx-native` : version native

Où `xxx` peut être `latest` ou un numéro de version (ex: `v1.1.1`).

## Fonctionnalités avancées

### Rappel de sommeil

KrabotNotif peut vous rappeler de faire dormir votre personnage chaque jour.

#### Fonctionnement

- Chaque jour à l'heure configurée (`JOB_SLEEP_SCHEDULER_TIME`, par défaut 20h00), le bot vérifie si l'action "Dormir" est disponible sur la page plateau
- Si le bouton "Dormir" est actif (classe `btn-primary`), une notification Discord est envoyée
- Cela permet de ne pas oublier de faire dormir son personnage avant la fin de journée

#### Configuration

| Variable | Description | Valeur par défaut |
|----------|-------------|-------------------|
| `JOB_SLEEP_SCHEDULER_TIME` | Heure de vérification (format HH:mm) | `20:00` |
| `DISCORD_SLEEP_MESSAGE` | Message de rappel | `N'oublie pas de dormir` |

### Userscript pour optimiser la vérification

KrabotNotif propose un userscript permettant de ne pas contacter Kraland pendant votre session de jeu.

#### Fonctionnement

L'userscript est accessible via : `http://<votre-backend>:8080/krabot/kramail-check/userscript.user.js`

Lorsque vous êtes connecté à www.kraland.org, le script :
- Détecte automatiquement votre visite sur le site
- Repousse le prochain scan de vérification de `JOB_KRALAND_SCHEDULER_DELAY` (par défaut 5 minutes)
- Évite ainsi les vérifications inutiles pendant que vous êtes actif sur Kraland et donc de déclencher la sécurité de KI.

#### Installation

1. Installer un gestionnaire de userscripts (Tampermonkey, Greasemonkey, Violentmonkey)
2. Accéder à l'URL du script : `http://<votre-backend>:8080/krabot/kramail-check/userscript.user.js`
3. Le gestionnaire proposera automatiquement l'installation
4. Accepter l'installation du script

Le script s'exécutera automatiquement à chaque visite sur Kraland.

### Gestion mémoire et Kubernetes

KrabotNotif inclut des fonctionnalités de gestion mémoire pour les déploiements Kubernetes :

- **Garbage Collection programmé** : exécution horaire configurable
- **Health Check mémoire** : probe surveillant l'utilisation mémoire
- **Seuils d'alerte** : warning à 80%, critique à 90%

Configuration détaillée : [KUBERNETES.md](KUBERNETES.md)

#### Variables mémoire

| Variable | Description | Valeur par défaut |
|----------|-------------|-------------------|
| `JOB_GC_SCHEDULER_CRON` | Planification du GC | `0 0 * ? * *` (chaque heure) |
| `MEMORY_WARNING_THRESHOLD` | Seuil warning (%) | `80` |
| `MEMORY_CRITICAL_THRESHOLD` | Seuil critique (%) | `90` |
| `JAVA_OPTS` | Options JVM | `-XX:+UseG1GC -XX:MaxRAMPercentage=75.0` |

## Versionning

Le projet suit [SemVer](https://semver.org/lang/fr/) : `MAJEUR.MINEUR.CORRECTIF`

- **MAJEUR** : changements non rétrocompatibles
- **MINEUR** : nouvelles fonctionnalités rétrocompatibles
- **CORRECTIF** : corrections rétrocompatibles

### Créer une release

1. Aller dans **Actions** sur GitHub
2. Sélectionner **Increment Version, Tag and Release**
3. Cliquer sur **Run workflow**
4. Choisir le type (`major`, `minor`, `patch`)

Le workflow :
- Incrémente la version et met à jour `pom.xml`
- Crée le tag `vX.Y.Z`
- Build les images Docker multi-architecture
- Pousse vers DockerHub (`latest` + version)
- Exécute les scans de sécurité Trivy
- Crée la release GitHub
