/*
 * Copyright (c) 2010, Andras Varga and Opensim Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Opensim Ltd. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Andras Varga or Opensim Ltd. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.omnetpp.scave.writers.example;

import org.omnetpp.scave.writers.IOutputVector;
import org.omnetpp.scave.writers.IResultManager;

public class AlohaServer extends Component {
    // state variables
    int numCurrentTransmissions = 0;
    boolean collision = false;
    double rxStartTime;

    // statistics
    long totalPackets = 0;
    double totalReceiveTime = 0; // non-collision
    double totalCollisionTime = 0;
    IOutputVector numCurrentTransmissionsVector;

    public AlohaServer(String name, SimulationManager sim, Component parent) {
        super(name, sim, parent);
        numCurrentTransmissionsVector = createOutputVector("numConcurrentTransmissions", null, null, IResultManager.IM_SAMPLE_HOLD);
        numCurrentTransmissionsVector.record(0);
    }

    public void packetReceptionStart() {
        if (numCurrentTransmissions == 0)
            rxStartTime = now();
        else
            collision = true;
        numCurrentTransmissions++;
        numCurrentTransmissionsVector.record(numCurrentTransmissions);

        totalPackets++;
    }

    protected void packetReceptionEnd() {
        numCurrentTransmissions--;
        numCurrentTransmissionsVector.record(numCurrentTransmissions);
        if (numCurrentTransmissions == 0) {
            if (collision)
                totalCollisionTime += now() - rxStartTime;
            else
                totalReceiveTime += now() - rxStartTime;
            collision = false;
        }
    }

    @Override
    protected void recordSummaryResults() {
        recordScalar("totalPackets", totalPackets);
        recordScalar("totalCollisionTime", totalCollisionTime, "s");
        recordScalar("totalReceiveTime", totalReceiveTime, "s");
        recordScalar("channelBusy", 100 * (totalReceiveTime+totalCollisionTime) / now(), "%");
        recordScalar("utilization", 100 * totalReceiveTime / now(), "%");
    }
}
