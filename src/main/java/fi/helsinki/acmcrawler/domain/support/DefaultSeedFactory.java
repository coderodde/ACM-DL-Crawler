package fi.helsinki.acmcrawler.domain.support;

import fi.helsinki.acmcrawler.domain.SeedFactory;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rodion Efremov
 * @version I
 */
public class DefaultSeedFactory implements SeedFactory<AuthorNode>{

    @Override
    public List<AuthorNode> get() {
        List<AuthorNode> list = new ArrayList<AuthorNode>();
        populate(list);
        return list;
    }

    private void populate(List<AuthorNode> list) {

    }
}
