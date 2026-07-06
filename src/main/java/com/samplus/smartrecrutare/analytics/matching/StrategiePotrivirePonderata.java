package com.samplus.smartrecrutare.analytics.matching;

import com.samplus.smartrecrutare.models.StarePotrivire;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

/** Strategie determinista bazata pe ponderile tiparului administrativ. */
@Component
public class StrategiePotrivirePonderata implements StrategiePotrivire {

    @Override
    public ScorPotrivire calculeaza(
            DateProfilPotrivire profil,
            DateJobPotrivire job,
            DateTiparPotrivire tipar
    ) {
        String textJob = normalizare(job.titlu() + " " + valoare(job.descriere()));
        int scorAbilitati = procentPotriviri(profil.abilitati(), textJob, false);
        int scorLocatie = scorPreferinta(profil.locatiiPreferate(), job.locatie());
        int scorContract = scorValoareOptionala(profil.tipContractPreferat(), job.tipContract());
        int scorCuvinte = procentPotriviri(profil.cuvinteCheie(), textJob, true);

        int total = Math.toIntExact(Math.round((
                (long) scorAbilitati * tipar.pondereAbilitati()
                        + (long) scorLocatie * tipar.pondereLocatie()
                        + (long) scorContract * tipar.pondereContract()
                        + (long) scorCuvinte * tipar.pondereCuvinteCheie()
        ) / 100.0));
        StarePotrivire stare = total >= tipar.pragNotificare()
                ? StarePotrivire.PESTE_PRAG
                : StarePotrivire.SUB_PRAG;

        return new ScorPotrivire(
                profil,
                job,
                tipar,
                total,
                scorAbilitati,
                scorLocatie,
                scorContract,
                scorCuvinte,
                stare
        );
    }

    private int procentPotriviri(Set<String> valori, String text, boolean optional) {
        if (valori.isEmpty()) {
            return optional ? 100 : 0;
        }
        long potriviri = valori.stream()
                .map(this::normalizare)
                .filter(text::contains)
                .count();
        return (int) Math.round(potriviri * 100.0 / valori.size());
    }

    private int scorPreferinta(Set<String> preferinte, String valoareJob) {
        if (preferinte.isEmpty()) {
            return 100;
        }
        String normalizat = normalizare(valoareJob);
        return preferinte.stream().map(this::normalizare).anyMatch(normalizat::contains) ? 100 : 0;
    }

    private int scorValoareOptionala(String preferinta, String valoareJob) {
        if (!StringUtils.hasText(preferinta)) {
            return 100;
        }
        return normalizare(preferinta).equals(normalizare(valoareJob)) ? 100 : 0;
    }

    private String valoare(String valoare) {
        return valoare == null ? "" : valoare;
    }

    private String normalizare(String valoare) {
        String faraAccente = Normalizer.normalize(valoare(valoare), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return faraAccente.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }
}
