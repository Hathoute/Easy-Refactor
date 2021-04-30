package allumettes;

/**
 * Lance une partie des 13 allumettes en fonction des arguments fournis sur la
 * ligne de commande.
 *
 * @author Xavier Crégut
 * @version $Revision: 1.5 $
 */
public class Jouer {

    /**
     * Lancer une partie. En argument sont donnés les deux joueurs sous la forme
     * nom@stratégie.
     *
     * @param args la description des deux joueurs
     */
    public static void main(String[] args) {
        try {
            verifierNombreArguments(args);
            Jeu jeu = new JeuReel(13);
            Joueur joueur1 = creerJoueur(args, 2);
            Joueur joueur2 = creerJoueur(args, 1);
            Arbitre arbitre = new Arbitre(joueur1, joueur2);
            arbitre.arbitrer(jeu);

        } catch (ConfigurationException e) {
            System.out.println();
            System.out.println("Erreur : " + e.getMessage());
            afficherUsage();
            System.exit(1);
        }
    }

    private static void verifierNombreArguments(String[] args) {
        final int nbJoueurs = 2;
        if (args.length < nbJoueurs) {
            throw new ConfigurationException("Trop peu d'arguments : " + args.length);
        }
        if (args.length > nbJoueurs + 1) {
            throw new ConfigurationException("Trop d'arguments : " + args.length);
        }
    }

    /** Afficher des indications sur la manière d'exécuter cette classe. */
    public static void afficherUsage() {
        System.out.println("\n" + "Usage :" + "\n\t" + "java allumettes.Jouer joueur1 joueur2" + "\n\t\t"
                + "joueur est de la forme nom@stratégie" + "\n\t\t"
                + "strategie = naif | rapide | expert | humain | tricheur" + "\n" + "\n\t" + "Exemple :" + "\n\t"
                + "	java allumettes.Jouer Xavier@humain " + "Ordinateur@naif" + "\n");
    }

    public static Strategie LaStrat(String strat) throws ConfigurationException {
        if (strat.equals("humain")) {
            return new StrategieHumain();
        } else if (strat.equals("lente")) {
            return new StrategieLente();
        } else if (strat.equals("rapide")) {
            return new StrategieRapide();
        } else if (strat.equals("naif")) {
            return new StrategieNaif();
        } else if (strat.equals("expert")) {
            return new StrategieExpert();
        } else {
            throw new ConfigurationException("Strategie inconnue");
        }
    }

    public static Joueur creerJoueur(String[] args, int n) {
        String[] j;
        j = spliter(args[args.length - n]);
        return new Joueur(j[0], LaStrat(j[1]));
    }

    public static String[] spliter(String args) {
        String[] j;
        j = args.split("@");
        return j;
    }

}
