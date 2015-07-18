/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.field;

import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.FieldSetting.Type;

/**
 * 경위도기반으로 거리를 계산한 필드값. Km단위이다.
 * @author swsong
 *
 */
public class DistanceField {
	public static final String fieldName = Type._DISTANCE.toString();
	public static final int fieldNumber = -5;
	public static final int fieldSize = IOUtil.SIZE_OF_INT; //float
	public static final FieldSetting field = new FieldSetting(fieldName, null, Type._DISTANCE);
}
