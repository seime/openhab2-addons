/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.millheat.internal.model;

import java.time.LocalDateTime;

/**
 * The {@link Mode} represents a mode with start and end time
 *
 * @author Arne Seime - Initial contribution
 */
public class Mode {
    private ModeType mode;
    private LocalDateTime start;
    private LocalDateTime end;

    public Mode(ModeType mode, LocalDateTime start, LocalDateTime end) {
        this.mode = mode;
        this.start = start;
        this.end = end;
    }

    public ModeType getMode() {
        return mode;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }
}
