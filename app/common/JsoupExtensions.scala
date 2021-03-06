/*
 * Copyright 2013-, Björn Westlin (bwestlin at gmail dot com) - github: bwestlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package common

import org.jsoup.nodes.Element
import org.jsoup.select.Elements

object JsoupExtensions {

  implicit class ElementsOps(val elements: Elements) extends AnyVal {
    def firstOpt: Option[Element] = {
      Option(elements.first())
    }
  }

  implicit class ElementOps(val element: Element) extends AnyVal {
    def nextElementSiblings(numSiblings: Int) = {
      def nextElementSiblingsIter(numLeft: Int, sibling: Element): List[Element] = {
        if (numLeft <= 0) Nil
        else {
          val nextSibling = sibling.nextElementSibling
          nextSibling :: nextElementSiblingsIter(numLeft - 1, nextSibling)
        }
      }
      nextElementSiblingsIter(numSiblings, element)
    }
  }
}
