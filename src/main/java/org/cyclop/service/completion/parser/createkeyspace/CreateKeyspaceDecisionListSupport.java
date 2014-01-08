package org.cyclop.service.completion.parser.createkeyspace;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.cyclop.model.CqlKeyword;
import org.cyclop.model.CqlQueryName;
import org.cyclop.service.completion.parser.CqlPartCompletion;
import org.cyclop.service.completion.parser.DecisionListSupport;

/**
 * @author Maciej Miklas
 */
@Named
public class CreateKeyspaceDecisionListSupport implements DecisionListSupport {

    private final CqlKeyword supports = CqlKeyword.Def.CREATE_KEYSPACE.value;

    private CqlPartCompletion[][] decisionList;

    @Inject
    private KeyspaceNameCompletion keyspaceNameCompletion;

    @Inject
    private AfterKeyspaceNameCompletion afterKeyspaceNameCompletion;

    @Inject
    private WithCompletion withCompletion;

    @PostConstruct
    public void init() {
        decisionList = new CqlPartCompletion[][]{{keyspaceNameCompletion}, {afterKeyspaceNameCompletion}, {withCompletion}};
    }

    @Override
    public CqlPartCompletion[][] getDecisionList() {
        return decisionList;
    }

    @Override
    public CqlKeyword supports() {
        return supports;
    }

    @Override
    public CqlQueryName queryName() {
        return CqlQueryName.CREATE_KEYSPACE;
    }

}
