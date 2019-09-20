      ****************************************************************************
      *                                                                          *
      * Copyright 2018-2019 ABSA Group Limited                                   *
      *                                                                          *
      * Licensed under the Apache License, Version 2.0 (the "License");          *
      * you may not use this file except in compliance with the License.         *
      * You may obtain a copy of the License at                                  *
      *                                                                          *
      *     http://www.apache.org/licenses/LICENSE-2.0                           *
      *                                                                          *
      * Unless required by applicable law or agreed to in writing, software      *
      * distributed under the License is distributed on an "AS IS" BASIS,        *
      * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
      * See the License for the specific language governing permissions and      *
      * limitations under the License.                                           *
      *                                                                          *
      ****************************************************************************

       01  R.
          03 N      PIC 9(1).
          03 A      OCCURS 0 TO 9 TIMES DEPENDING ON N.
            05  B1  PIC X(1).
            05  B2  PIC X(1).
          03 C      PIC X(1).