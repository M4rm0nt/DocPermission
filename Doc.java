package docs;

import java.util.*;
import java.util.logging.Logger;

public class Doc {

    private static final Logger LOGGER = Logger.getLogger(Doc.class.getName());

    enum Berechtigung {
        LESEN, SCHREIBEN, LOESCHEN
    }

    static class Gruppe {
        private int id;
        private String name;
        private List<Benutzer> benutzer;
        private Set<Berechtigung> berechtigungen;

        public Gruppe(int id, String name) {
            this.id = id;
            this.name = name;
            this.benutzer = new ArrayList<>();
            this.berechtigungen = new HashSet<>();
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Benutzer> getBenutzer() {
            return benutzer;
        }

        public void setBenutzer(List<Benutzer> benutzer) {
            this.benutzer = benutzer;
        }

        public Set<Berechtigung> getBerechtigungen() {
            return berechtigungen;
        }

        public void setBerechtigungen(Set<Berechtigung> berechtigungen) {
            this.berechtigungen = berechtigungen;
        }

        public void addBerechtigung(Berechtigung berechtigung) {
            this.berechtigungen.add(berechtigung);
        }

    }

    static class Benutzer {
        private int id;
        private String name;
        private Set<Gruppe> gruppen;

        public Benutzer(int id, String name) {
            this.id = id;
            this.name = name;
            this.gruppen = new HashSet<>();
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Set<Gruppe> getGruppen() {
            return gruppen;
        }

        public void setGruppen(Set<Gruppe> gruppen) {
            this.gruppen = gruppen;
        }

        public void addGruppe(Gruppe gruppe) {
            this.gruppen.add(gruppe);
            gruppe.getBenutzer().add(this);
        }
    }

    static class Dokument {
        private int id;
        private String name;
        private Benutzer eigentümer;
        private Map<Gruppe, Set<Berechtigung>> berechtigungen;

        public Dokument(int id, String name, Benutzer eigentümer) {
            this.id = id;
            this.name = name;
            this.eigentümer = eigentümer;
            this.berechtigungen = new HashMap<>();
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Benutzer getEigentümer() {
            return eigentümer;
        }

        public void setEigentümer(Benutzer eigentümer) {
            this.eigentümer = eigentümer;
        }

        public Map<Gruppe, Set<Berechtigung>> getBerechtigungen() {
            return berechtigungen;
        }

        public void setBerechtigungen(Map<Gruppe, Set<Berechtigung>> berechtigungen) {
            this.berechtigungen = berechtigungen;
        }

        public void addBerechtigung(Gruppe gruppe, Berechtigung berechtigung) {
            berechtigungen.computeIfAbsent(gruppe, k -> new HashSet<>()).add(berechtigung);
        }
    }

    static class BerechtigungsManager {
        private static BerechtigungsManager instance;

        private BerechtigungsManager() {}

        public static BerechtigungsManager getInstance() {
            if (instance == null) {
                instance = new BerechtigungsManager();
            }
            return instance;
        }

        public boolean hatBerechtigung(Benutzer benutzer, Dokument dokument, Berechtigung berechtigung) {
            if (dokument.getEigentümer().equals(benutzer)) {
                return true;
            }

            for (Gruppe gruppe : benutzer.getGruppen()) {
                if (hatGruppeBerechtigung(gruppe, dokument, berechtigung)) {
                    return true;
                }
            }
            return false;
        }

        private boolean hatGruppeBerechtigung(Gruppe gruppe, Dokument dokument, Berechtigung berechtigung) {
            return dokument.getBerechtigungen().containsKey(gruppe) &&
                    dokument.getBerechtigungen().get(gruppe).contains(berechtigung);
        }

        public void logZugriff(Benutzer benutzer, Dokument dokument, Berechtigung berechtigung, boolean erlaubt) {
            LOGGER.info(String.format("Benutzer %s hat versucht, auf Dokument %s zuzugreifen. Berechtigung: %s. Erlaubt: %b",
                    benutzer.getName(), dokument.getName(), berechtigung, erlaubt));
        }

    }

    public static void main(String[] args) {
        BerechtigungsManager manager = BerechtigungsManager.getInstance();

        Benutzer admin = new Benutzer(1, "Admin");
        Benutzer user = new Benutzer(2, "User");

        Gruppe gruppeHRadmin = new Gruppe(1, "HR Admin");
        gruppeHRadmin.addBerechtigung(Berechtigung.LESEN);
        gruppeHRadmin.addBerechtigung(Berechtigung.SCHREIBEN);
        gruppeHRadmin.addBerechtigung(Berechtigung.LOESCHEN);

        Gruppe gruppeHRuser = new Gruppe(2, "HR User");
        gruppeHRuser.addBerechtigung(Berechtigung.LESEN);

        admin.addGruppe(gruppeHRadmin);
        user.addGruppe(gruppeHRuser);

        Dokument dokument = new Dokument(1, "Gehaltsabrechnung", admin);
        dokument.addBerechtigung(gruppeHRadmin, Berechtigung.SCHREIBEN);
        dokument.addBerechtigung(gruppeHRuser, Berechtigung.LESEN);

        boolean kannLesen;
        boolean kannSchreiben;
        boolean kannLoeschen;

        // Admin
        kannLesen = manager.hatBerechtigung(admin, dokument, Berechtigung.LESEN);
        manager.logZugriff(admin, dokument, Berechtigung.LESEN, kannLesen);
        if(kannLesen) {
            System.out.println("Admin kann lesen: " + kannLesen);
        } else {
            System.out.println("Admin kann nicht lesen: " + kannLesen);
        }

        kannSchreiben = manager.hatBerechtigung(admin, dokument, Berechtigung.SCHREIBEN);
        manager.logZugriff(admin, dokument, Berechtigung.SCHREIBEN, kannSchreiben);
        if(kannSchreiben) {
            System.out.println("Admin kann schreiben: " + kannSchreiben);
        } else {
            System.out.println("Admin kann nicht schreiben: " + kannSchreiben);
        }

        kannLoeschen = manager.hatBerechtigung(admin, dokument, Berechtigung.LOESCHEN);
        manager.logZugriff(admin, dokument, Berechtigung.LOESCHEN, kannLoeschen);
        if(kannLoeschen) {
            System.out.println("Admin kann löschen: " + kannLoeschen);
        } else {
            System.out.println("Admin kann nicht löschen: " + kannLoeschen);
        }

        // User
        kannLesen = manager.hatBerechtigung(user, dokument, Berechtigung.LESEN);
        manager.logZugriff(user, dokument, Berechtigung.LESEN, kannLesen);
        if(kannLesen) {
            System.out.println("User kann lesen: " + kannLesen);
        } else {
            System.out.println("User kann nicht lesen: " + kannLesen);
        }

        kannSchreiben = manager.hatBerechtigung(user, dokument, Berechtigung.SCHREIBEN);

        manager.logZugriff(user, dokument, Berechtigung.SCHREIBEN, kannSchreiben);
        if(kannSchreiben) {
            System.out.println("User kann schreiben: " + kannSchreiben);
        } else {
            System.out.println("User kann nicht schreiben: " + kannSchreiben);
        }

        kannLoeschen = manager.hatBerechtigung(user, dokument, Berechtigung.LOESCHEN);
        manager.logZugriff(user, dokument, Berechtigung.LOESCHEN, kannLoeschen);
        if(kannLoeschen) {
            System.out.println("User kann löschen: " + kannLoeschen);
        } else {
            System.out.println("User kann nicht löschen: " + kannLoeschen);
        }

    }
}