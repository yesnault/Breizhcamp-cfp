Breizhcamp-cfp
==============

BreizhCamp - Call for paper

## Deploiement Cloudbees

* installer le SDK cloudbees
* configurer le ficher ~/.bees/bees.config ( voir http://developer.cloudbees.com/bin/view/RUN/Playframework)

* ajouter les variables d'environement

<pre><code>
    $ export MYSQL_URL_DB=na
    $ export MYSQL_USERNAME_DB=na
    $ export MYSQL_PASSWORD_DB=na
    $ export SENDGRID_SMTP_HOST=na
    $ export SENDGRID_USERNAME=na
    $ export SENDGRID_PASSWORD=na
</code></pre>

* deployer l'application sur cloudbees

<pre><code>
    $ play
    $ cloudbees-deploy-config cloudbees cfp
</code></pre>

## En local

### Pour lancer l'application en local avec la base h2

<pre><code>
    $ play run
</code></pre>


### Pour lancer l'application en local avec une base de test sur cloudbees,
* Remplacer "na" par les informations de connexion à la base

<pre><code>
    $ export MYSQL_URL_DB=na
    $ export MYSQL_USERNAME_DB=na
    $ export MYSQL_PASSWORD_DB=na
    $ export SENDGRID_SMTP_HOST=smtp.gmail.com
    $ export SENDGRID_USERNAME=username
    $ export SENDGRID_PASSWORD=password
</code></pre>

<pre><code>
    $ play -Dconf.file=conf/cloudbees.conf run
</code></pre>