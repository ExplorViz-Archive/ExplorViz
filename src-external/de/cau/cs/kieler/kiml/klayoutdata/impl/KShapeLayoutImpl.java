/**
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 * 
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2009 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 *
 * $Id$
 */
package de.cau.cs.kieler.kiml.klayoutdata.impl;

import de.cau.cs.kieler.core.kgraph.impl.KGraphDataImpl;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.kiml.klayoutdata.KInsets;
import de.cau.cs.kieler.kiml.klayoutdata.KLayoutDataPackage;
import de.cau.cs.kieler.kiml.klayoutdata.KShapeLayout;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>KShape Layout</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.cau.cs.kieler.kiml.klayoutdata.impl.KShapeLayoutImpl#getXpos <em>Xpos</em>}</li>
 *   <li>{@link de.cau.cs.kieler.kiml.klayoutdata.impl.KShapeLayoutImpl#getYpos <em>Ypos</em>}</li>
 *   <li>{@link de.cau.cs.kieler.kiml.klayoutdata.impl.KShapeLayoutImpl#getWidth <em>Width</em>}</li>
 *   <li>{@link de.cau.cs.kieler.kiml.klayoutdata.impl.KShapeLayoutImpl#getHeight <em>Height</em>}</li>
 *   <li>{@link de.cau.cs.kieler.kiml.klayoutdata.impl.KShapeLayoutImpl#getInsets <em>Insets</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class KShapeLayoutImpl extends KGraphDataImpl implements KShapeLayout {
    /**
     * The default value of the '{@link #getXpos() <em>Xpos</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getXpos()
     * @generated
     * @ordered
     */
    protected static final float XPOS_EDEFAULT = 0.0F;

    /**
     * The cached value of the '{@link #getXpos() <em>Xpos</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getXpos()
     * @generated
     * @ordered
     */
    protected float xpos = XPOS_EDEFAULT;

    /**
     * The default value of the '{@link #getYpos() <em>Ypos</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getYpos()
     * @generated
     * @ordered
     */
    protected static final float YPOS_EDEFAULT = 0.0F;

    /**
     * The cached value of the '{@link #getYpos() <em>Ypos</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getYpos()
     * @generated
     * @ordered
     */
    protected float ypos = YPOS_EDEFAULT;

    /**
     * The default value of the '{@link #getWidth() <em>Width</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getWidth()
     * @generated
     * @ordered
     */
    protected static final float WIDTH_EDEFAULT = 0.0F;

    /**
     * The cached value of the '{@link #getWidth() <em>Width</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getWidth()
     * @generated
     * @ordered
     */
    protected float width = WIDTH_EDEFAULT;

    /**
     * The default value of the '{@link #getHeight() <em>Height</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getHeight()
     * @generated
     * @ordered
     */
    protected static final float HEIGHT_EDEFAULT = 0.0F;

    /**
     * The cached value of the '{@link #getHeight() <em>Height</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getHeight()
     * @generated
     * @ordered
     */
    protected float height = HEIGHT_EDEFAULT;

    /**
     * The cached value of the '{@link #getInsets() <em>Insets</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getInsets()
     * @generated
     * @ordered
     */
    protected KInsets insets;
    
    /**
     * <!-- begin-user-doc -->
     * Whether the position or size has been modified.
     * <!-- end-user-doc -->
     * @generated NOT
     */
    private boolean modified = false;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected KShapeLayoutImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return KLayoutDataPackage.Literals.KSHAPE_LAYOUT;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public float getXpos() {
        return xpos;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated NOT
     */
    public void setXpos(float newXpos) {
        float oldXpos = xpos;
        xpos = newXpos;
        if (newXpos != oldXpos) {
            modified = true;
        }
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, KLayoutDataPackage.KSHAPE_LAYOUT__XPOS, oldXpos, xpos));
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public float getYpos() {
        return ypos;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated NOT
     */
    public void setYpos(float newYpos) {
        float oldYpos = ypos;
        ypos = newYpos;
        if (newYpos != oldYpos) {
            modified = true;
        }
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, KLayoutDataPackage.KSHAPE_LAYOUT__YPOS, oldYpos, ypos));
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public float getWidth() {
        return width;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated NOT
     */
    public void setWidth(float newWidth) {
        float oldWidth = width;
        width = newWidth;
        if (newWidth != oldWidth) {
            modified = true;
        }
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, KLayoutDataPackage.KSHAPE_LAYOUT__WIDTH, oldWidth, width));
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public float getHeight() {
        return height;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated NOT
     */
    public void setHeight(float newHeight) {
        float oldHeight = height;
        height = newHeight;
        if (newHeight != oldHeight) {
            modified = true;
        }
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, KLayoutDataPackage.KSHAPE_LAYOUT__HEIGHT, oldHeight, height));
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public KInsets getInsets() {
        return insets;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetInsets(KInsets newInsets, NotificationChain msgs) {
        KInsets oldInsets = insets;
        insets = newInsets;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, KLayoutDataPackage.KSHAPE_LAYOUT__INSETS, oldInsets, newInsets);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setInsets(KInsets newInsets) {
        if (newInsets != insets) {
            NotificationChain msgs = null;
            if (insets != null)
                msgs = ((InternalEObject)insets).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - KLayoutDataPackage.KSHAPE_LAYOUT__INSETS, null, msgs);
            if (newInsets != null)
                msgs = ((InternalEObject)newInsets).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - KLayoutDataPackage.KSHAPE_LAYOUT__INSETS, null, msgs);
            msgs = basicSetInsets(newInsets, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, KLayoutDataPackage.KSHAPE_LAYOUT__INSETS, newInsets, newInsets));
    }

    /**
     * <!-- begin-user-doc -->
     * {@inheritDoc}
     * <!-- end-user-doc -->
     * @generated NOT
     */
    public void setPos(float newXpos, float newYpos) {
        float oldXpos = xpos, oldYpos = ypos;
        xpos = newXpos;
        ypos = newYpos;
        if (newXpos != oldXpos || newYpos != oldYpos) {
            modified = true;
        }
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, KLayoutDataPackage.KSHAPE_LAYOUT__YPOS, oldYpos, ypos));
            eNotify(new ENotificationImpl(this, Notification.SET, KLayoutDataPackage.KSHAPE_LAYOUT__XPOS, oldXpos, xpos));
        }
    }

    /**
     * <!-- begin-user-doc -->
     * {@inheritDoc}
     * <!-- end-user-doc -->
     * @generated NOT
     */
    public void applyVector(KVector pos) {
        setPos((float) pos.x, (float) pos.y);
    }

    /**
     * <!-- begin-user-doc -->
     * {@inheritDoc}
     * <!-- end-user-doc -->
     * @generated NOT
     */
    public KVector createVector() {
        return new KVector(xpos, ypos);
    }

    /**
     * <!-- begin-user-doc -->
     * {@inheritDoc}
     * <!-- end-user-doc -->
     * @generated NOT
     */
    public void setSize(float newWidth, float newHeight) {
        float oldWidth = width, oldHeight = height;
        width = newWidth;
        height = newHeight;
        if (newWidth != oldWidth || newHeight != oldHeight) {
            modified = true;
        }
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, KLayoutDataPackage.KSHAPE_LAYOUT__WIDTH, oldWidth, width));
            eNotify(new ENotificationImpl(this, Notification.SET, KLayoutDataPackage.KSHAPE_LAYOUT__HEIGHT, oldHeight, height));
        }
    }
    
    /**
     * <!-- begin-user-doc -->
     * {@inheritDoc}
     * <!-- end-user-doc -->
     * @generated NOT
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * <!-- begin-user-doc -->
     * {@inheritDoc}
     * <!-- end-user-doc -->
     * @generated NOT
     */
    public void resetModificationFlag() {
        modified = false;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case KLayoutDataPackage.KSHAPE_LAYOUT__INSETS:
                return basicSetInsets(null, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case KLayoutDataPackage.KSHAPE_LAYOUT__XPOS:
                return getXpos();
            case KLayoutDataPackage.KSHAPE_LAYOUT__YPOS:
                return getYpos();
            case KLayoutDataPackage.KSHAPE_LAYOUT__WIDTH:
                return getWidth();
            case KLayoutDataPackage.KSHAPE_LAYOUT__HEIGHT:
                return getHeight();
            case KLayoutDataPackage.KSHAPE_LAYOUT__INSETS:
                return getInsets();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case KLayoutDataPackage.KSHAPE_LAYOUT__XPOS:
                setXpos((Float)newValue);
                return;
            case KLayoutDataPackage.KSHAPE_LAYOUT__YPOS:
                setYpos((Float)newValue);
                return;
            case KLayoutDataPackage.KSHAPE_LAYOUT__WIDTH:
                setWidth((Float)newValue);
                return;
            case KLayoutDataPackage.KSHAPE_LAYOUT__HEIGHT:
                setHeight((Float)newValue);
                return;
            case KLayoutDataPackage.KSHAPE_LAYOUT__INSETS:
                setInsets((KInsets)newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case KLayoutDataPackage.KSHAPE_LAYOUT__XPOS:
                setXpos(XPOS_EDEFAULT);
                return;
            case KLayoutDataPackage.KSHAPE_LAYOUT__YPOS:
                setYpos(YPOS_EDEFAULT);
                return;
            case KLayoutDataPackage.KSHAPE_LAYOUT__WIDTH:
                setWidth(WIDTH_EDEFAULT);
                return;
            case KLayoutDataPackage.KSHAPE_LAYOUT__HEIGHT:
                setHeight(HEIGHT_EDEFAULT);
                return;
            case KLayoutDataPackage.KSHAPE_LAYOUT__INSETS:
                setInsets((KInsets)null);
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case KLayoutDataPackage.KSHAPE_LAYOUT__XPOS:
                return xpos != XPOS_EDEFAULT;
            case KLayoutDataPackage.KSHAPE_LAYOUT__YPOS:
                return ypos != YPOS_EDEFAULT;
            case KLayoutDataPackage.KSHAPE_LAYOUT__WIDTH:
                return width != WIDTH_EDEFAULT;
            case KLayoutDataPackage.KSHAPE_LAYOUT__HEIGHT:
                return height != HEIGHT_EDEFAULT;
            case KLayoutDataPackage.KSHAPE_LAYOUT__INSETS:
                return insets != null;
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated NOT
     */
    @Override
    public String toString() {
        return "(" + xpos + "," + ypos + " | " + width + "," + height + ")";
    }

} //KShapeLayoutImpl
