package mockit.asm.exceptionHandling;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mockit.asm.constantPool.ConstantPoolGeneration;
import mockit.asm.controlFlow.Edge;
import mockit.asm.controlFlow.FrameTypeMask;
import mockit.asm.controlFlow.Label;
import mockit.asm.util.ByteVector;

import org.checkerframework.checker.index.qual.NonNegative;

public final class ExceptionHandling {
    @Nonnull
    private final List<ExceptionHandler> handlers;
    @Nonnull
    private final ConstantPoolGeneration cp;

    public ExceptionHandling(@Nonnull ConstantPoolGeneration cp) {
        handlers = new ArrayList<>();
        this.cp = cp;
    }

    public void addHandler(@Nonnull Label start, @Nonnull Label end, @Nonnull Label handler, @Nullable String type) {
        int handlerType = type == null ? 0 : cp.newClass(type);
        handlers.add(new ExceptionHandler(start, end, handler, type, handlerType));
    }

    public void completeControlFlowGraphWithExceptionHandlerBlocksFromComputedFrames() {
        for (ExceptionHandler exceptionHandler : handlers) {
            Label handler = exceptionHandler.handler.getFirst();
            Label start = exceptionHandler.start.getFirst();
            Label end = exceptionHandler.end.getFirst();

            // Computes the kind of the edges to 'handler'.
            String catchType = exceptionHandler.getCatchTypeDesc();
            int kindOfEdge = FrameTypeMask.OBJECT | cp.addNormalType(catchType);

            // 'handler' is an exception handler.
            handler.markAsTarget();

            addHandlerLabelAsSuccessor(kindOfEdge, handler, start, end);
        }
    }

    public void completeControlFlowGraphWithExceptionHandlerBlocks() {
        for (ExceptionHandler exceptionHandler : handlers) {
            addHandlerLabelAsSuccessor(Edge.EXCEPTION, exceptionHandler.handler, exceptionHandler.start,
                    exceptionHandler.end);
        }
    }

    // Adds 'handler' as a successor of labels between 'start' and 'end'.
    private static void addHandlerLabelAsSuccessor(int kindOfEdge, @Nonnull Label handler, @Nonnull Label start,
            @Nonnull Label end) {
        while (start != end) {
            Edge edge = new Edge(kindOfEdge, handler);
            // noinspection ConstantConditions
            start = start.setSuccessors(edge);
        }
    }

    public boolean hasHandlers() {
        return !handlers.isEmpty();
    }

    @NonNegative
    public int getSize() {
        return 8 * handlers.size();
    }

    public void put(@Nonnull ByteVector out) {
        out.putShort(handlers.size());

        for (ExceptionHandler exceptionHandler : handlers) {
            exceptionHandler.put(out);
        }
    }
}
