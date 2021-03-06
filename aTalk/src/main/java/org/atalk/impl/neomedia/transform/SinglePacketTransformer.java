/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package org.atalk.impl.neomedia.transform;

import org.atalk.service.neomedia.ByteArrayBuffer;
import org.atalk.service.neomedia.RawPacket;
import org.atalk.util.function.Predicate;

import timber.log.Timber;

/**
 * Extends the <tt>PacketTransformer</tt> interface with methods which allow the transformation of a
 * single packet into a single packet.
 *
 * Eases the implementation of <tt>PacketTransformer<tt>-s which transform each
 * packet into a single transformed packet (as opposed to an array of possibly more than one packet).
 *
 * cmeng (20190723): Do not update this file - video call not working on old android (Note-3)
 * Need API-24 for new implementation
 *
 * @author Boris Grozev
 * @author George Politis
 * @author Eng Chong Meng
 */
public abstract class SinglePacketTransformer implements PacketTransformer
{
    /**
     * The number of <tt>Throwable</tt>s to log with a single call to <tt>logger</tt>. If every
     * <tt>Throwable</tt> is logged in either of {@link #reverseTransform(RawPacket)} and
     * {@link #transform(RawPacket)}, the logging may be overwhelming.
     */
    private static final int EXCEPTIONS_TO_LOG = 1000;

    /**
     * The number of exceptions caught in {@link #reverseTransform(RawPacket)}.
     */
    // private AtomicInteger exceptionsInReverseTransform = new AtomicInteger();
    private long exceptionsInReverseTransform;


    /**
     * The number of exceptions caught in {@link #transform(RawPacket)}.
     */
    // private AtomicInteger exceptionsInTransform = new AtomicInteger();
    private long exceptionsInTransform;

    /**
     * The idea is to have <tt>PacketTransformer</tt> implementations strictly associated with a
     * <tt>Predicate</tt> so that they only process packets that they're supposed to process. For
     * example, transformers that transform RTP packets should not transform RTCP packets, if, by
     * mistake, they happen to be passed RTCP packets.
     */
    private final Predicate<ByteArrayBuffer> packetPredicate;

    /*
     * A cached link to {@link #reverseTransform(RawPacket)} method to reduce calling overhead on hotpath.
     * Need API-24 (not use in aTalk)
     */
    // private final Function<RawPacket, RawPacket> cachedReverseTransform
    //        = pkt -> this.reverseTransform(pkt);

    /*
     * A cached link to {@link #transform(RawPacket)} method to reduce calling overhead on hotpath.
     * Need API-24 (not use in aTalk)
     */
    // private final Function<RawPacket, RawPacket> cachedTransform
    //         = pkt -> this.transform(pkt);

    /**
     * Ctor.
     *
     * XXX At some point ideally we would get rid of this ctor and all the inheritors will use the
     * parametrized ctor. Also, we might want to move this check inside the
     * <tt>TransformEngineChain</tt> so that we only make the check once per packet: The RTCP
     * transformer is only supposed to (reverse) transform RTCP packets and the RTP transformer is
     * only supposed to modify RTP packets.
     */
    public SinglePacketTransformer()
    {
        this(null);
    }

    /**
     * Ctor.
     *
     * @param packetPredicate the <tt>PacketPredicate</tt> to use to match packets to (reverse) transform.
     */
    public SinglePacketTransformer(Predicate<ByteArrayBuffer> packetPredicate)
    {
        this.packetPredicate = packetPredicate;
    }

    /**
     * {@inheritDoc}
     *
     * The (default) implementation of {@code SinglePacketTransformer} does nothing.
     */
    @Override
    public void close()
    {
    }

    /**
     * Reverse-transforms a specific packet.
     *
     * @param pkt the transformed packet to be restored.
     * @return the restored packet.
     */
    public abstract RawPacket reverseTransform(RawPacket pkt);

    /**
     * {@inheritDoc}
     *
     * Reverse-transforms an array of packets by calling {@link #reverseTransform(RawPacket)} on each one.
     * cmeng (20190723): Do not update this method - video call not working on old android (Note-3)
     */
    @Override
    public RawPacket[] reverseTransform(RawPacket[] pkts)
    {
        // For API-24 only
        // return transformArray(pkts, cachedReverseTransform, exceptionsInReverseTransform, "reverseTransform");

        if (pkts != null) {
            for (int i = 0; i < pkts.length; i++) {
                RawPacket pkt = pkts[i];

                if (pkt != null && (packetPredicate == null || packetPredicate.test(pkt))) {
                    try {
                        pkts[i] = reverseTransform(pkt);
                    } catch (Throwable t) {
                        exceptionsInReverseTransform++;
                        if ((exceptionsInReverseTransform % EXCEPTIONS_TO_LOG) == 0 || exceptionsInReverseTransform == 1) {
                            Timber.e(t, "Failed to reverse-transform RawPacket(s)!");
                        }
                        if (t instanceof Error)
                            throw (Error) t;
                        else
                            throw (RuntimeException) t;
                    }
                }
            }
        }
        return pkts;
    }

    /**
     * Transforms a specific packet.
     *
     * @param pkt the packet to be transformed.
     * @return the transformed packet.
     */
    public abstract RawPacket transform(RawPacket pkt);

    /**
     * {@inheritDoc}
     *
     * Transforms an array of packets by calling {@link #transform(RawPacket)} on each one.
     */
    @Override
    public RawPacket[] transform(RawPacket[] pkts)
    {
        // For API-24 only
        // return transformArray(pkts, cachedTransform, exceptionsInTransform, "transform");

        if (pkts != null) {
            for (int i = 0; i < pkts.length; i++) {
                RawPacket pkt = pkts[i];

                if (pkt != null && (packetPredicate == null || packetPredicate.test(pkt))) {
                    try {
                        pkts[i] = transform(pkt);
                    } catch (Throwable t) {
                        exceptionsInTransform++;
                        if ((exceptionsInTransform % EXCEPTIONS_TO_LOG) == 0 || exceptionsInTransform == 1) {
                            Timber.e(t, "Failed to transform RawPacket(s)!");
                        }
                        if (t instanceof Error)
                            throw (Error) t;
                        else
                            throw (RuntimeException) t;
                    }
                }
            }
        }
        return pkts;
    }

    // cmeng - for API-24 implementation
    /*
     * Applies a specific transformation function to an array of {@link RawPacket}s.
     *
     * @param pkts the array to transform.
     * @param transformFunction the function to apply to each (non-null) element of the array.
     * @param exceptionCounter a counter of the number of exceptions encountered.
     * @param logMessage a name of the transformation function, to be used when logging exceptions.
     * @return {@code pkts}.
     */
//    private RawPacket[] transformArray(RawPacket[] pkts, Function<RawPacket, RawPacket> transformFunction,
//            AtomicInteger exceptionCounter, String logMessage)
//    {
//        if (pkts != null) {
//            for (int i = 0; i < pkts.length; i++) {
//                RawPacket pkt = pkts[i];
//                if (pkt != null
//                        && (packetPredicate == null || packetPredicate.test(pkt))) {
//                    try {
//                        pkts[i] = transformFunction.apply(pkt);
//                    } catch (Throwable t) {
//                        exceptionCounter.incrementAndGet();
//                        if ((exceptionCounter.get() % EXCEPTIONS_TO_LOG) == 0
//                                || exceptionCounter.get() == 1) {
//                            Timber.e(t, "Failed to %s RawPacket(s)!", logMessage);
//                        }
//                        if (t instanceof Error) {
//                            throw (Error) t;
//                        }
//                        else {
//                            throw (RuntimeException) t;
//                        }
//                    }
//                }
//            }
//        }
//        return pkts;
//    }
}
