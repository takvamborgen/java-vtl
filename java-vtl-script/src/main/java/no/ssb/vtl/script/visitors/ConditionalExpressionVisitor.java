package no.ssb.vtl.script.visitors;

import no.ssb.vtl.model.Component;
import no.ssb.vtl.model.DataStructure;
import no.ssb.vtl.model.VTLExpression;
import no.ssb.vtl.model.VTLObject;
import no.ssb.vtl.parser.VTLBaseVisitor;
import no.ssb.vtl.parser.VTLParser;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.Map;
import java.util.Optional;

import static java.lang.String.*;

public class ConditionalExpressionVisitor extends VTLBaseVisitor<VTLExpression> {

    private final ReferenceVisitor referenceVisitor;
    private final DataStructure dataStructure;

    public ConditionalExpressionVisitor(ReferenceVisitor referenceVisitor, DataStructure dataStructure) {
        this.referenceVisitor = referenceVisitor;
        this.dataStructure = dataStructure;
    }

    @Override
    public VTLExpression visitNvlExpression(VTLParser.NvlExpressionContext ctx) {
        ParamVisitor paramVisitor = new ParamVisitor(referenceVisitor);
        Component input = (Component) paramVisitor.visit(ctx.componentRef());
        Object repValue = paramVisitor.visit(ctx.nvlRepValue);

        if (input.getType() != repValue.getClass()) {
            throw new ParseCancellationException("The value to replace null must be of type " + input.getType());
        }

        return new VTLExpression.Builder(input.getType(), dataPoint -> {
            Map<Component, VTLObject> map = dataStructure.asMap(dataPoint);
            Optional<VTLObject> vtlObject = Optional.ofNullable(map.get(input));
            if (!vtlObject.isPresent() || vtlObject.get().get() == null) {
                return VTLObject.of(repValue);
            } else {
                return vtlObject.get();
            }

        }).description(format("nvl(%s, %s)", input, repValue)).build();
    }
}