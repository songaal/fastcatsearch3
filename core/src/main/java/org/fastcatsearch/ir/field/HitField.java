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
 * 필드에 키워드가 매칭되면 count를 1씩 증가하는 방법으로 얻어진 점수.
 * 
 * @author swsong
 * 
 */
public class HitField {
	public static final String fieldName = Type._HIT.toString();
	public static final int fieldNumber = -3;
	public static final int fieldSize = IOUtil.SIZE_OF_INT;
	public static final FieldSetting field = new FieldSetting(fieldName, null, Type._HIT);
}
