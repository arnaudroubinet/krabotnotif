Krabot notif est une application Quarkus utilisant les webhook discord.
Elle va :
- Se connecter à www.kraland.org
- Vérifier toutes les minutes si vous avez reçu un message dans votre rapport ou un nouveau kramail
- Vous envoyez une notification sur discord le cas échéant
- Il ne renverra pas deux fois la même notification pour le même événement sauf si vous relancez le robot

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
DISCORD_ERROR_PREFIX_MESSAGE: Le message en prefix d'une exception
JOB_SCHEDULER_EVERY : La récurrence du job, la valeur par défaut est 60s
```

Pour DISCORD_KRAMAIL_MESSAGE, vous pouvez rajouter "\*originator\*" et "\*title\*" dans le message.
Ces balises, serrons remplacées par les valeurs du message.
