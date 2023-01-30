package com.example.myapplication; // Organise le code en paquet

import androidx.appcompat.app.AppCompatActivity; // Import les outils nécéssaires au fonctionnement de la class

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity { // Création d'une class MainActivity, étendu à AppCompatActivity qui fournit les outils

    private static final long DEBUT_TEMPS_MILLIS = 60000; // Constante pour le début du temps 60000 millisecondes = 1 minute

    private CountDownTimer compteurTemps; // Compteur pour le temps

    private boolean modeInverse; // Variable pour savoir le mode du jeu (orientation normale ou inverse)

    private long tempsRestant = DEBUT_TEMPS_MILLIS; // Temps restant dans le compteur

    private Button boutonNormal, boutonInverse, boutonSortie; // Boutons du menu

    private final int capteurType = Sensor.TYPE_GYROSCOPE; // Capteur de mouvements (gyroscope)

    private FrameLayout balle; // Balle dans le jeu

    private ImageView trou, centreTrou; // Trou dans le jeu
    private ImageView fond; // Image de fond pour l'accueil

    private float xPosition, xAcceleration, xVelocite = 0.0f; // Variables d'accéleration sur l'axe x
    private float yPosition, yAcceleration, yVelocite = 0.0f; // Variables d'accéleration sur l'axe y
    private float xMaximum, yMaximum; // Taille de l'écran (appreil)

    private final Rect rectangleBalle = new Rect (); // Variable de collision de la balle
    private final Rect rectangleTrou = new Rect (); // Variable de collision du trou

    private TextView scoreBalle, scoreRecord, scoreRecordInverse; // Affichage du scores sur la balle et des meilleurs scores sur la page d'accueil

    private int points = 0; // Nombre de points dans la partie courante

    private boolean jeuLance = false; // Vérifie si on a lancé une partie

    private ProgressBar mProgressBar; // Barre de progression pour afficher le temps restant

    private MediaPlayer sonPoint; // Effet de son quand on marque un point

    @SuppressLint ("SetTextI18n")
    @Override
    protected void onCreate (Bundle savedInstanceState) { // Méthode de création des éléments pour le jeu avec le paramètre savedInstanceState

        super.onCreate (savedInstanceState); // Surcharge la méthode savedInstanceState

        setContentView (R.layout.activity_main); // Charge l'activity_main

        getWindow ().addFlags (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Active l'écran tant que l'application est en marche

        boutonNormal = findViewById(R.id.buttonStart); // Création d'un bouton normal
        boutonInverse = findViewById(R.id.buttonInverse); // création d'un bouton inverse
        boutonSortie = findViewById(R.id.buttonExit);  // création d'un bouton sortie

        scoreRecordInverse = findViewById (R.id.scoreRecordInverse); // Création d'un score pour le mode inverse
        scoreRecord = findViewById (R.id.scoreRecord); // Création d'un score pour le mode normal

        balle = findViewById (R.id.ball); // Création d'une balle
        scoreBalle = findViewById (R.id.scoreBalle); // Création du score de la balle

        trou = findViewById (R.id.trou);  // Création du trou

        mProgressBar = findViewById (R.id.progressBar); // Création d'une barre de progression

        fond = findViewById (R.id.fond); // Création d'un fond

        centreTrou = findViewById (R.id.centreTrou); // Création du centre du trou

        sonPoint = MediaPlayer.create (this, R.raw.point); // Création du son pour chaque point marqué

        mProgressBar.setMax (100); // Met à 100 % la barre de pregression

        scoreBalle.setVisibility (View.INVISIBLE); // Affiche le score sur la balle

        mProgressBar.setVisibility (View.INVISIBLE); // Affiche la barre de progression

        balle.setVisibility (View.INVISIBLE); // Affiche la balle

        trou.setVisibility (View.INVISIBLE); // Affiche le trou

        centreTrou.setVisibility (View.INVISIBLE); // Affiche le centre du trou

        fond.setVisibility (View.VISIBLE); // Affiche le fond

        Point size = new Point (); // Crée un nouveau point

        Display display = getWindowManager ().getDefaultDisplay (); // Récupère la taille de l'écran

        display.getSize (size); // Donne la taille de l'écran

        xMaximum = (float) size.y - 400; // Limite la taille horizontale, pour éviter qu'on sorte de l'écran
        yMaximum = (float) size.x - 200; // Limite la taille verticale, pour éviter qu'on sorte de l'écran

        SharedPreferences prefs = getSharedPreferences ("scores", Context.MODE_PRIVATE); // Affiche le meilleur score, au lancement du jeu

        int score = prefs.getInt ("normal", 0); // Valeur par défaut (0)

        scoreRecord.setText ("Record Score Normal : " + score); // Affiche le meilleur score du mode normal

        score = prefs.getInt ("inverse", 0); // Valeur par défaut (0)

        scoreRecordInverse.setText ("Record Score Inversé : " + score); // Affiche le meilleur score du mode inversé

        final SensorManager sensorManager = (SensorManager) getSystemService (Context.SENSOR_SERVICE); // Initialise le gestionnaire de capteurs

        sensorManager.registerListener (accelerometreListener, sensorManager.getDefaultSensor (capteurType), SensorManager.SENSOR_DELAY_UI); // Mise en place d'un écouteur pour un capteurType donné (enregistrement d'un listener)

        boutonNormal.setOnClickListener (v -> { // Clique sur le bouton du mode orientation normale. on lance le jeu en orientation normale avec l'utilisation du lambda

            jeuLance = true; // Lance le jeu

            modeInverse = false; // Ne lance pas le mode inversé

            mProgressBar.setProgress (0); // Valeur de départ (0), pour la barre de progression

            boutonNormal.setVisibility (View.INVISIBLE); // Affiche le bouton normal
            boutonInverse.setVisibility (View.INVISIBLE); // Affiche le bouton inverse
            boutonSortie.setVisibility (View.INVISIBLE); // Affiche le bouton sortie

            scoreRecord.setVisibility (View.INVISIBLE); // Affiche le meilleur score du mode normal
            scoreRecordInverse.setVisibility (View.INVISIBLE); // Affiche le meilleur score du mode inversé

            fond.setVisibility (View.INVISIBLE); // Affiche le fond

            int x = (int) (Math.random () * (int) xMaximum); // Calcul la position horizontale de la balle
            int y = (int) (Math.random () * (int) yMaximum); // Calcul la position verticale de la balle

            balle.setY (y); // Position verticale aléatoire de la balle
            balle.setX (x); // Position horizontale aléatoire de la balle

            x = (int) (Math.random () * (int) xMaximum); // Calcul la position horizontale du trou
            y = (int) (Math.random () * (int) yMaximum); // Calcul la position verticale du trou

            trou.setY (x); // Position verticale aléatoire du trou
            trou.setX (y);  // Position horizontale aléatoire du trou

            centreTrou.setY (x + 100); // Position verticale aléatoire pour le centre du trou
            centreTrou.setX (y + 100); // Position horizontale aléatoire pour le centre du trou

            points = 0; // Initiale les points (0)

            mProgressBar.setVisibility (View.VISIBLE); // Affiche la barre de progression

            balle.setVisibility (View.VISIBLE); // Affiche la balle
            scoreBalle.setVisibility (View.VISIBLE); // Affiche le score sur la balle

            trou.setVisibility (View.VISIBLE); // Affiche le trou
            centreTrou.setVisibility (View.VISIBLE); // Affiche le centre du trou

            startTemps (); // Lance le temps
        });

        boutonInverse.setOnClickListener (v -> { // Clique sur le bouton oriertation inversée, on lance le jeu en mode inversée avec l'utilisation du lambda

            jeuLance = true; // Lance le jeu

            modeInverse = true; // Lance le mode inversé


            mProgressBar.setProgress (0); // Valeur de départ (0), pour la barre de progression

            boutonNormal.setVisibility (View.INVISIBLE); // Affiche le bouton normal
            boutonInverse.setVisibility (View.INVISIBLE); // Affiche le bouton inverse
            boutonSortie.setVisibility (View.INVISIBLE); // Affiche le bouton sortie

            scoreRecord.setVisibility (View.INVISIBLE); // Affiche le meilleur score du mode normal
            scoreRecordInverse.setVisibility (View.INVISIBLE); // Affiche le meilleur score du mode inversé

            fond.setVisibility (View.INVISIBLE); // Affiche le fond

            int x = (int) (Math.random () * (int) xMaximum); // Calcul la position horizontale de la balle
            int y = (int) (Math.random () * (int) yMaximum); // Calcul la position verticale de la balle

            balle.setY (x); // Position verticale aléatoire de la balle
            balle.setX (y); // Position horizontale aléatoire de la balle

            x = (int) (Math.random () * (int) xMaximum); // Calcul la position horizontale du trou
            y = (int) (Math.random () * (int) yMaximum); // Calcul la position verticale du trou

            trou.setY (x); // Position verticale aléatoire du trou
            trou.setX (y); // Position horizontale aléatoire du trou

            centreTrou.setY (x + 100); // Position verticale aléatoire pour le centre du trou
            centreTrou.setX (y + 100); // Position horizontale aléatoire pour le centre du trou

            points = 0; // Initiale les points (0)

            mProgressBar.setVisibility (View.VISIBLE); // Affiche la barre de progression

            balle.setVisibility (View.VISIBLE); // Affiche la balle
            scoreBalle.setVisibility (View.VISIBLE); // Affiche le score sur la balle

            trou.setVisibility (View.VISIBLE); // Affiche le trou
            centreTrou.setVisibility (View.VISIBLE); // Affiche le centre du trou

            startTemps (); // Lance le temps
        });

        boutonSortie.setOnClickListener (v -> { // Clique sur le bouton sortie, on quitte l'application (jeu) avec l'utilisation du lambda

            finish (); // Arrète le temps

            System.exit (0); // Sort de l'application
        });
    }

    private final SensorEventListener accelerometreListener = new SensorEventListener () { // Méthode pour la capture de nouvelles données du capteur (événement)

        @SuppressLint ("SetTextI18n")
        @Override
        public void onSensorChanged (SensorEvent sensorEvent) { // Récupère les informations sur les 2 axes (x, y)

            if (jeuLance) { // Lance le jeu

                if (modeInverse) { // Orientation inversée (mode)

                    xAcceleration = sensorEvent.values [0] * 5; // Accélération horizontale
                    yAcceleration = sensorEvent.values [1] * 5; // Accélération verticale

                } else { // Orientation normale (mode)

                    xAcceleration = - sensorEvent.values [0] * 5; // Accélération horizontale
                    yAcceleration = - sensorEvent.values [1] * 5;  // Accélération verticale
                }

                bouger (); // Bouge la balle

                balle.getHitRect (rectangleBalle); // Vérifier la collision de la balle

                centreTrou.getHitRect (rectangleTrou); // Vérifier la collision pour le centre du trou

                if (Rect.intersects (rectangleBalle, rectangleTrou)) { // Vérifie la collision  de la balle avec le centre du trou, pour marquer un point

                    sonPoint.start (); // Lance le son du point
                    points++;

                    scoreBalle.setText ("" + points); // Met à jour l'affichage des points sur la balle

                    int x = (int) (Math.random () * (int) xMaximum); // Calcul la position horizontale du trou
                    int y = (int) (Math.random () * (int) yMaximum); // Calcul la position horizontale du trou

                    trou.setX (y); // Position verticale aléatoire du trou
                    trou.setY (x); // Position horizontale aléatoire du trou

                    centreTrou.setY (x + 100); // Position verticale aléatoire pour le centre du trou
                    centreTrou.setX (y + 100); // Position horizontale aléatoire pour le centre du trou
                }
            }
        }

        @Override
        public void onAccuracyChanged (Sensor sensor, int i) { // Méthode pour la précision des capteurs
        }
    };

    public void bouger () { // Méthode pour bouger la balle

        float frameTime = 2f; // Temps de frame (affichage de l'image)

        xVelocite += (xAcceleration * frameTime); // Calcul la vélocité horizontale
        yVelocite += (yAcceleration * frameTime); // Calcul la vélocité verticale

        xPosition -= (xVelocite / 2) * frameTime; // Calcul la postion horizontale
        yPosition -= (yVelocite / 2) * frameTime; // Calcul la postion verticale

        if (xPosition >= xMaximum) { // Vérifie la position horizontale, si la balle sort de l'écran

            xPosition = xMaximum;

        } else if (xPosition < 0) { // Vérifie la position horizontale, si la balle ne sort pas de l'écran

            xPosition = 0;
        }

        if (yPosition >= yMaximum) { // Vérifie la position verticale, si la balle sort de l'écran

            yPosition = yMaximum;

        } else if (yPosition < 0) { // Vérifie la position verticale, si la balle ne sort pas de l'écran

            yPosition = 0;
        }

        balle.setY (xPosition); // Position horizontale aléatoire de la balle
        balle.setX (yPosition); // Position verticale aléatoire de la balle
    }

    private void startTemps () { // Méthode qui lance la durée de la partie

        compteurTemps = new CountDownTimer (tempsRestant, 1000) { // Compteur pour le temps, qui arrète la partie au bout de 1 minute

            @Override
            public void onTick (long millisUntilFinished) { /* Méthode qui se lance automatiquement le compteur
            pour le temps */

                tempsRestant = millisUntilFinished; // Temps restant en secondes

                mProgressBar.setProgress ((int) (((double) tempsRestant / 60000) * 100)); // Diminue la barre de progression
            }

            @SuppressLint ("SetTextI18n")
            @Override
            public void onFinish () { // Méthode qui termine la partie

                balle.setVisibility (View.INVISIBLE); // Affiche la balle

                trou.setVisibility (View.INVISIBLE); // Affiche le trou
                centreTrou.setVisibility (View.INVISIBLE); // Affiche le centre du trou

                mProgressBar.setVisibility (View.INVISIBLE); // Affiche la barre de progression
                mProgressBar.setProgress (0); // Valeur de départ (0), pour la barre de progression

                resetTemps (); // Réinitialise le temps du compteur

                verifierPoints (); // Vérifie le nombre de points

                fond.setVisibility (View.VISIBLE); // Affiche le fond

                boutonNormal.setVisibility (View.VISIBLE); // Affiche le bouton normal
                boutonInverse.setVisibility (View.VISIBLE); // Affiche le bouton inverse
                boutonSortie.setVisibility (View.VISIBLE); // Affiche le bouton sortie

                scoreRecord.setVisibility (View.VISIBLE); // Affiche le meilleur score du mode normal
                scoreRecordInverse.setVisibility (View.VISIBLE); // Affiche le meilleur score du mode inversé

                points = 0; // Initiale les points (0)

                jeuLance = false; // Lance pas le jeu

                scoreBalle.setText ("" + points); // Met à jour l'affichage des points sur la balle
            }
        }.start (); // Lance le temps
    }

    private void resetTemps () { // Méthode pour remettre le compteur à 0

        tempsRestant = DEBUT_TEMPS_MILLIS; // Temps restant en secondes
    }

    @SuppressLint("SetTextI18n")
    private void verifierPoints () { // Méthode qui met à jour les scores, en prenant le meilleur score

        if (!modeInverse) { // Lance le mode normal avec la négation (!)

            SharedPreferences prefs = getSharedPreferences ("scores", Context.MODE_PRIVATE); // Affiche le meilleur scores, au lancement du jeu

            int score = prefs.getInt ("normal", 0); // Valeur par défaut (0)

            if (points > score) { // Compare les points marqués avec le meilleur score

                SharedPreferences.Editor editor = prefs.edit (); // Change le meilleur score

                editor.putInt ("normal", points); // change le nombre (entier)

                editor.apply (); // Applique un changement

                scoreRecord.setText ("Score Record Normal : " + points); // Affiche le meilleur score du mode normal
            }
        }

        if (modeInverse) { // Lance le mode inverse

            SharedPreferences prefs = getSharedPreferences ("scores", Context.MODE_PRIVATE); // Affiche le meilleur score, au lancement du jeu

            int score = prefs.getInt ("inverse", 1); // Valeur par défaut (1)

            if (points > score) { // Compare les points marqués avec le meilleur score

                SharedPreferences.Editor editor = prefs.edit (); // Change le meilleur score

                editor.putInt ("inverse", points); // change le nombre (entier)

                editor.apply (); // Applique un changement

                scoreRecordInverse.setText ("Score Record Inversé : " + points); // Affiche le meilleur score du mode inversé
            }
        }
    }
}