/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ofbiz.sql;

public abstract class Value extends Atom {
    public static final class Null extends Value {
        private Null() {
        }
        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        public StringBuilder appendTo(StringBuilder sb) {
            return sb.append("NULL");
        }
    }

    public static final Null NULL = new Null();

    public interface Visitor {
        void visit(AggregateFunction value);
        void visit(FieldValue value);
        void visit(FunctionCall value);
        void visit(MathValue value);
        void visit(Null value);
        void visit(NumberValue<?> value);
        void visit(ParameterValue value);
        void visit(StringValue value);
        void visit(CountAllFunction value);
    }

    public static class BaseVisitor implements Visitor {
        public void visit(AggregateFunction value) {
        }

        public void visit(FieldValue value) {
        }

        public void visit(FunctionCall value) {
        }

        public void visit(MathValue value) {
        }

        public void visit(Null value) {
        }

        public void visit(NumberValue<?> value) {
        }

        public void visit(ParameterValue value) {
        }

        public void visit(StringValue value) {
        }

        public void visit(CountAllFunction value) {
        }
    }

    public abstract void accept(Visitor visitor);
}
